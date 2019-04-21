package ca.joelathiessen.kaly2.core.server

import ca.joelathiessen.kaly2.core.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.core.map.GlobalMap
import ca.joelathiessen.kaly2.core.map.MapTree
import ca.joelathiessen.kaly2.core.odometry.CarModel
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.persistence.PersistentStorage
import ca.joelathiessen.kaly2.core.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.core.planner.linear.LinearPathSegmentRootFactory
import ca.joelathiessen.kaly2.core.ev3.DistSensorComm
import ca.joelathiessen.kaly2.core.ev3.RobotComm
import ca.joelathiessen.kaly2.core.ev3.RobotInfo
import ca.joelathiessen.kaly2.core.ev3.SerialConnectionCreator
import ca.joelathiessen.kaly2.core.ev3.TextMessenger
import ca.joelathiessen.kaly2.core.slam.FastSLAM
import ca.joelathiessen.kaly2.core.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.core.slam.NNDataAssociator
import ca.joelathiessen.kaly2.core.subconscious.LocalPlan
import ca.joelathiessen.kaly2.core.subconscious.LocalPlanner
import ca.joelathiessen.kaly2.core.subconscious.PilotPoses
import ca.joelathiessen.kaly2.core.subconscious.RealPilotPoses
import ca.joelathiessen.kaly2.core.subconscious.RobotPilot
import ca.joelathiessen.kaly2.core.subconscious.sensor.Kaly2Sensor
import ca.joelathiessen.kaly2.core.subconscious.sensor.Spinnable
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicBoolean

class RealRobotSessionFactory(
    private val wheelRadius: Float,
    private val maxDriveAngle: Float,
    private val maxSpeedRadsPerSec: Float,
    private val robotSize: Float,
    private val maxRot: Float,
    private val distToSpeedConv: Float,
    private val defaultNumParticles: Int,
    private val defaultDistVariance: Float,
    private val defaultAngVariance: Float,
    private val defaultIdentityVariance: Float,
    private val lineThreshold: Float,
    private val checkWithinAngle: Float,
    private val maxRatio: Float,
    private val localPlannerRotStep: Float,
    private val localPlannerDistStep: Float,
    private val localPlannerGridStep: Float,
    private val localPlannerGridSize: Float,
    private val obstacleSize: Float,
    private val globalPlannerSearchDist: Float,
    private val globalPlannerStepDist: Float,
    private val globalPlannerItrs: Int,
    private val mapRemoveInvalidObsInterval: Int,
    private val minSubcMeasTime: Long,
    private val nnAsocThreshold: Float,
    private val persistentStorage: PersistentStorage,
    private val robotSerialCreator: SerialConnectionCreator,
    private val sensorSerialCreator: SerialConnectionCreator
) : RobotSessionFactory {
    private val ROBOT_NAME = "realRobot"
    private val MAP_NAME = "defaultMap"

    override fun makeRobotSession(sid: Long?, sessionStoppedWithNoSubscribersHandler: (stopSid: Long) -> Unit): RobotSessionFactoryResult {
        val currentDate = DateTime()

        val robotStorage = persistentStorage.getOrMakeRobotStorage(sid, ROBOT_NAME, true, MAP_NAME, currentDate)

        if (robotStorage == null) {
            if (sid != null) {
                val serverAddress = persistentStorage.getServerAddress(sid)
                if (serverAddress != null) {
                    return RobotSessionFactoryResult.RemoteRobotSessionAddress(serverAddress)
                }
            }
            return RobotSessionFactoryResult.RobotSessionCreationError()
        } else {
            val initalGoalPose = RobotPose(0L, 0f, 0f, 0f, 0f)

            val startPose = initalGoalPose

            val obstacles = MapTree()

            val robotTextMessenger = TextMessenger(robotSerialCreator)
            val robotComm = RobotComm(robotTextMessenger)

            val realPilot = RealPilot(robotComm, wheelRadius, maxDriveAngle, maxSpeedRadsPerSec, distToSpeedConv, startPose, maxRot)
            val realSpinner = RealSpinner(robotComm)

            val sensorMessenger = TextMessenger(sensorSerialCreator)
            val sensorComm = DistSensorComm(sensorMessenger)
            val realSensor = RealSensor(sensorComm)

            val featureDetector = SplitAndMerge(lineThreshold, checkWithinAngle, maxRatio)

            val motionModel = CarModel()
            val dataAssoc = NNDataAssociator(nnAsocThreshold)
            val partResamp = FastUnbiasedResampler()
            val slam = FastSLAM(startPose, motionModel, dataAssoc, partResamp, defaultNumParticles, defaultDistVariance, defaultAngVariance, defaultIdentityVariance)

            val localPlanner = LocalPlanner(robotSize, localPlannerRotStep, localPlannerDistStep, localPlannerGridStep,
                    localPlannerGridSize, obstacleSize)

            val factory = LinearPathSegmentRootFactory()
            val globalPathPlanner = GlobalPathPlanner(factory, obstacles, obstacleSize, globalPlannerSearchDist, globalPlannerStepDist,
                    startPose, initalGoalPose, globalPlannerItrs)

            val map = GlobalMap(obstacleSize, obstacleSize, mapRemoveInvalidObsInterval)

            return RobotSessionFactoryResult.LocalRobotSession(RobotSession(robotStorage.sid,
                    sessionStoppedWithNoSubscribersHandler, startPose, initalGoalPose, realPilot, realSpinner,
                    realSensor, featureDetector, minSubcMeasTime, map, robotStorage, slam, localPlanner, globalPathPlanner))
        }
    }
}

class RealPilot(
    private val robotComm: RobotComm,
    private val wheelRadius: Float,
    private val maxDriveAngle: Float,
    private val maxSpeedRadsPerSec: Float,
    private val distToSpeedConv: Float,
    startPose: RobotPose,
    override val maxDesiredPlanRot: Float
) : RobotPilot {
    override val maxDesiredPlanDist: Float
        get() = maxSpeedRadsPerSec * wheelRadius
    override val poses: PilotPoses
        get() = synchronized(pilotPosesLock) { pilotPoses }
    private val pilotPosesLock = Any()
    private var pilotPoses = RealPilotPoses(startPose)
    private var prevInfo: RobotInfo? = null

    init {
        robotComm.subscribeToRobotInfo { info ->
            val pose = synchronized(pilotPosesLock) {
                RobotPose(pilotPoses.odoPose)
            }
            val lastTravelAngle = prevInfo?.travelAngle ?: 0f
            val distance = info.travelAngle - lastTravelAngle * wheelRadius
            pose.arcUpdate(distance, info.steerAngle)
            synchronized(pilotPosesLock) {
                pilotPoses = RealPilotPoses(pose)
            }
        }
    }

    override fun execLocalPlan(plan: LocalPlan) {
        val driveSpeed = Math.max(plan.distance * distToSpeedConv, maxSpeedRadsPerSec)
        val driveAngle = Math.max(plan.angle, maxDriveAngle)
        robotComm.driveAtAngle(driveAngle, driveSpeed)
    }
}

class RealSpinner(private val robotCommsManager: RobotComm) : Spinnable {
    private val isSpinning = AtomicBoolean(false)
    private val isTurningClockwise = AtomicBoolean(false)
    override val spinning: Boolean
        get() = isSpinning.get()
    override val turningClockwise: Boolean
        get() = isTurningClockwise.get()

    init {
        robotCommsManager.subscribeToRobotInfo { info ->
            isSpinning.set(info.sensorSpinning)
        }
    }

    override fun spin() {
        robotCommsManager.spinSensor()
    }
}

class RealSensor(sensorCommsManager: DistSensorComm) : Kaly2Sensor {
    private val distLock = Any()
    private var dist: Float = 0f

    init {
        sensorCommsManager.subscribeToDistSensorInfo { distIn ->
            synchronized(distLock) {
                dist = distIn
            }
        }
    }

    override fun fetchSample(sample: FloatArray, offset: Int) {
        return synchronized(distLock) {
            dist
        }
    }
}
