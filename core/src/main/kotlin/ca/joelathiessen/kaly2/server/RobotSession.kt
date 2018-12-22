package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.RobotCoreActed
import ca.joelathiessen.kaly2.RobotCoreActedResults
import ca.joelathiessen.kaly2.RobotCoreActor
import ca.joelathiessen.kaly2.RobotCoreRsltsMsg
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.FeatureDetector
import ca.joelathiessen.kaly2.map.GlobalMap
import ca.joelathiessen.kaly2.odometry.AccurateSlamOdometry
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.persistence.RobotStorage
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.PlannerActor
import ca.joelathiessen.kaly2.server.messages.RTFeature
import ca.joelathiessen.kaly2.server.messages.RTMsg
import ca.joelathiessen.kaly2.server.messages.RTParticle
import ca.joelathiessen.kaly2.server.messages.PastSlamInfosResponse
import ca.joelathiessen.kaly2.server.messages.PastSlamInfosRequest
import ca.joelathiessen.kaly2.server.messages.RTPose
import ca.joelathiessen.kaly2.server.messages.RobotSessionSettingsRequest
import ca.joelathiessen.kaly2.server.messages.RTSlamInfoMsg
import ca.joelathiessen.kaly2.server.messages.SlamSettingsResponse
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.subconscious.LocalPlanner
import ca.joelathiessen.kaly2.subconscious.RobotPilot
import ca.joelathiessen.kaly2.subconscious.SimPilotPoses
import ca.joelathiessen.kaly2.subconscious.SubconsciousActed
import ca.joelathiessen.kaly2.subconscious.SubconsciousActor
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2Sensor
import ca.joelathiessen.kaly2.subconscious.sensor.Spinnable
import ca.joelathiessen.util.EventContainer
import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.ItrActorMsg
import ca.joelathiessen.util.itractor.ItrActorThreadedHost
import lejos.robotics.navigation.Pose
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class SlamSettings(val numParticles: Int, val sensorAngVar: Float, val sensorDistVar: Float) : ItrActorMsg()

class RobotSession(
    val sid: Long,
    private val sessionStoppedWithNoSubscribersHandler: (stopSid: Long) -> Unit,
    private val startPose: RobotPose,
    initialGoal: RobotPose,
    robotPilot: RobotPilot,
    spinner: Spinnable,
    sensor: Kaly2Sensor,
    featureDetector: FeatureDetector,
    minSubcMeasTime: Long,
    map: GlobalMap,
    private val robotStorage: RobotStorage,
    slam: FastSLAM,
    localPlanner: LocalPlanner,
    globalPathPlanner: GlobalPathPlanner
) {

    private val SUBCONC_INPUT_SIZE = 0
    private val PLANNER_INPUT_SIZE = 0
    private val ROBOT_CORE_INPUT_SIZE = 0
    private val ROBOT_CORE_OUTPUT_SIZE = 0

    private val rtUpdateEventCont = EventContainer<RTMsg>()
    private val rtUpdateEvent = rtUpdateEventCont.event

    private val updateExecutor = Executors.newSingleThreadExecutor()!!
    private val shouldRun = AtomicBoolean()
    private val robotIsRunningLock = Any()
    private val resultsLock = Any()
    private var results: RobotCoreActedResults? = null

    private val rtEventSubscriptionLock = Any()

    private val subconscInput = ItrActorChannel(SUBCONC_INPUT_SIZE)
    private val globalPathPlannerInput = ItrActorChannel(PLANNER_INPUT_SIZE)
    private val robotCoreInput = ItrActorChannel(ROBOT_CORE_INPUT_SIZE)
    private val robotCoreOutput = ItrActorChannel(ROBOT_CORE_OUTPUT_SIZE)

    private val subConscActorHost: ItrActorThreadedHost
    private val plannerActorHost: ItrActorThreadedHost
    private val robotCoreActorHost: ItrActorThreadedHost

    init {
        val gblManeuvers: List<RobotPose> = ArrayList()
        val accurateOdo = AccurateSlamOdometry(robotPilot.poses.odoPose, { robotPilot.poses.odoPose })

        val subConsc = SubconsciousActed(robotPilot, accurateOdo, localPlanner,
                sensor, spinner, gblManeuvers, minSubcMeasTime)

        val robotCore = RobotCoreActed(initialGoal, accurateOdo, slam, featureDetector, map, robotStorage)

        val subConscActor = SubconsciousActor(subConsc, subconscInput, robotCoreInput)
        val plannerActor = PlannerActor(globalPathPlanner, globalPathPlannerInput, robotCoreInput)
        val robotCoreActor = RobotCoreActor(robotCore, robotCoreInput, robotCoreOutput, globalPathPlannerInput, subconscInput)

        subConscActorHost = ItrActorThreadedHost(subConscActor)
        plannerActorHost = ItrActorThreadedHost(plannerActor)
        robotCoreActorHost = ItrActorThreadedHost(robotCoreActor)
    }

    private lateinit var simThread: Thread

    fun makeSimThread(): Thread = thread(start = false) {

        synchronized(robotIsRunningLock) {
            subConscActorHost.start()
            plannerActorHost.start()
            robotCoreActorHost.start()

            println("Robot $sid started...")

            while (shouldRun.get()) {
                val localResults = (robotCoreOutput.takeMsg() as RobotCoreRsltsMsg).results

                synchronized(resultsLock) {
                    results = localResults
                }

                val simPilotPoses = localResults.subconcResults.pilotPoses as SimPilotPoses

                sendUpdateEvent(simPilotPoses.realPose, simPilotPoses.odoPose, localResults.particlePoses,
                        localResults.features, localResults.numItrs, localResults)
            }
            subConscActorHost.stop()
            plannerActorHost.stop()
            robotCoreActorHost.stop()
            println("...Robot $sid stopped")
        }
    }

    private fun sendUpdateEvent(
        truePos: Pose,
        odoPos: Pose,
        particlePoses: List<Pose>,
        featuresForRT: List<Feature>,
        iteration: Long,
        results: RobotCoreActedResults
    ) {

        // send non-blocking events:
        updateExecutor.execute {
            val rtParticlePoses = particlePoses.map {
                RTParticle(it.x, it.y, it.heading, ArrayList())
            }
            val rtFeatures = featuresForRT.map { RTFeature(it.distance, it.angle, it.stdDev) }
            val rtOdoPos = RTPose(odoPos.x, odoPos.y, odoPos.heading)
            val rtTruePos = RTPose(truePos.x, truePos.y, truePos.heading)

            var sumX = 0.0f
            var sumY = 0.0f
            var sumHeading = 0.0f
            particlePoses.forEach {
                sumX += it.x
                sumY += it.y
                sumHeading += it.heading
            }
            val rtBestPose = RTPose(sumX / particlePoses.size, sumY / particlePoses.size,
                    sumHeading / particlePoses.size)

            val rtMsg = RTMsg(RTSlamInfoMsg(sid, iteration, System.currentTimeMillis(), rtParticlePoses, rtFeatures, rtBestPose,
                    rtOdoPos, rtTruePos))

            val rtFullMsg = RTMsg(results, requestingNoNetworkSend = true)

            synchronized(rtEventSubscriptionLock) {
                rtUpdateEventCont(this, rtMsg)
                rtUpdateEventCont(this, rtFullMsg)
            }
        }
    }

    @Synchronized
    fun subscribeToRTEvents(handler: (sender: Any, eventArgs: RTMsg) -> Unit) {
        synchronized(rtEventSubscriptionLock) {
            rtUpdateEvent += handler
        }
    }

    @Synchronized
    fun unsubscribeFromRTEvents(handler: ((sender: Any, eventArgs: RTMsg) -> Unit)? = null) {
        synchronized(rtEventSubscriptionLock) {
            if (handler != null) {
                rtUpdateEvent -= handler
            } else {
                rtUpdateEvent.clear()
            }
            if (rtUpdateEvent.length == 0) {
                stopRobot() // just stop immediately for now...
                sessionStoppedWithNoSubscribersHandler(sid)
            }
        }
    }

    /**
     * Starts the robot if it is not already started
     */
    @Synchronized
    fun startRobot() {
        if (shouldRun.get() == false || simThread.isAlive == false) {
            synchronized(robotIsRunningLock) {
                shouldRun.set(true)
                simThread = makeSimThread()
                simThread.start()
            }
        }
    }

    @Synchronized
    fun stopRobot() {
        shouldRun.set(false)
    }

    // All these public facing methods should be synchronized
    @Synchronized
    fun getAvgPose(): RobotPose {
        synchronized(resultsLock) {
            val resultsObt = results
            return when (resultsObt) {
                is RobotCoreActedResults -> resultsObt.slamPose
                else -> startPose
            }
        }
    }

    @Synchronized
    fun applySlamSettings(settingsMsg: SlamSettingsResponse) {
        robotCoreInput.addMsg(SlamSettings(settingsMsg.numParticles, settingsMsg.sensorAngVar, settingsMsg.sensorDistVar))
    }

    @Synchronized
    fun applyRobotSessionSettings(rSettingsRobotReq: RobotSessionSettingsRequest): Boolean {
        if (rSettingsRobotReq.shouldRun) {
            startRobot()
        } else {
            stopRobot()
        }
        return shouldRun.get()
    }

    @Synchronized
    fun getPastIterations(pastItrsReq: PastSlamInfosRequest): PastSlamInfosResponse {
        val itrs = robotStorage.getIterations(pastItrsReq.firstItr, pastItrsReq.lastItr)
        val slamInfos = itrs.map { sInfo ->
            val particles = sInfo.particles.map { part ->
                RTParticle(part)
            }
            val features = sInfo.features.map { feat ->
                RTFeature(feat)
            }
            val slamPose = RTPose(sInfo.slamPose)
            val odoPose = RTPose(sInfo.odoPose)

            val realPose = if ( sInfo.realPose != null) RTPose(sInfo.realPose) else null
            RTSlamInfoMsg(sid, sInfo.itrNum, sInfo.timestamp, particles, features, slamPose, odoPose, realPose)
        }

        return PastSlamInfosResponse(slamInfos)
    }

    @Synchronized
    fun attemptHeartBeat(): Boolean {
        return robotStorage.saveHeartbeat()
    }

    @Synchronized
    fun releaseSessionHistory() {
        robotStorage.releaseSessionHistory()
    }
}