package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.server.messages.FastSlamInfo
import ca.joelathiessen.kaly2.server.messages.FastSlamSettingsMsg
import ca.joelathiessen.kaly2.server.messages.RTFeature
import ca.joelathiessen.kaly2.server.messages.RTLandmark
import ca.joelathiessen.kaly2.server.messages.RTMsg
import ca.joelathiessen.kaly2.server.messages.RTParticle
import ca.joelathiessen.kaly2.server.messages.RTPose
import ca.joelathiessen.kaly2.server.messages.RobotSettingsMsg
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.subconscious.sensor.SensorInfo
import ca.joelathiessen.util.EventContainer
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.FloatRandom
import ca.joelathiessen.util.getFeatureForPosition
import lejos.robotics.navigation.Pose
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class RobotHandler(val rid: Long) {
    val NUM_LANDMARKS = 11
    val MIN_TIMESTEP = 33 // Don't wait for shorter than this (in ms) before starting the next simulation timestep
    val ROT_RATE = 0.03f
    val STEP_DIST = 2f
    val STEP_ROT_STD_DEV = 0.01f
    val STEP_DIST_STD_DEV = 0.5f
    val MIN_WIDTH = 400.0f
    val SENSOR_DIST_STD_DEV = 0.001f
    val SENSOR_ANG_STD_DEV = 0.001f
    val ODO_DIST_STD_DEV = 0.01f
    val ODO_ANG_STD_DEV = 0.01f

    val startPos = RobotPose(0, 0f, MIN_WIDTH / 2f, MIN_WIDTH / 2f, 0f)

    private val rtUpdateEventCont = EventContainer<RTMsg>()
    val rtUpdateEvent = rtUpdateEventCont.event

    private val updateExecutor = Executors.newSingleThreadExecutor()!!
    private val shouldRun = AtomicBoolean()
    private var shouldPause = AtomicBoolean(false)
    private val pauseSemaphore = Semaphore(1)
    private val robotIsRunningLock = Any()

    private lateinit var slam: FastSLAM
    private val motionModel = CarModel()
    private val dataAssoc = NNDataAssociator()
    private val partResamp = FastUnbiasedResampler()
    private val sensorInfo = object : SensorInfo {}
    private val random = FloatRandom(1)

    private data class xyPnt(val x: Float, val y: Float)

    private val realObjectLocs = ArrayList<xyPnt>()

    init {
        for (i in 1..NUM_LANDMARKS) {
            realObjectLocs += xyPnt(random.nextFloat() * MIN_WIDTH, random.nextFloat() * MIN_WIDTH)
        }
    }

    private lateinit var simThread: Thread

    fun makeSimThread(): Thread = thread(start = false) {

        // for now, instead of running an entire robot, run just its FastSLAM simulation

        synchronized(robotIsRunningLock) {
            println("Robot $rid started...")
            slam = FastSLAM(startPos, motionModel, dataAssoc, partResamp, sensorInfo)

            var x = MIN_WIDTH / 2
            var y = MIN_WIDTH / 2
            var theta = 0.1f

            var odoX = x
            var odoY = y
            var odoTheta = theta

            var times = 0L
            val odoLocs = ArrayList<RobotPose>()
            val realLocs = ArrayList<RobotPose>()

            while (shouldRun.get()) {
                val startTime = System.currentTimeMillis()

                // move the robot
                val dTheta = ROT_RATE + (STEP_ROT_STD_DEV * random.nextGaussian())
                theta += dTheta
                odoTheta += dTheta + (ODO_ANG_STD_DEV * random.nextGaussian())
                val distCommon = STEP_DIST + (STEP_DIST_STD_DEV * random.nextGaussian())
                val odoOffsetCommon = distCommon + (ODO_DIST_STD_DEV * random.nextGaussian())
                x += FloatMath.cos(theta) * distCommon
                odoX += FloatMath.cos(odoTheta) * (odoOffsetCommon)
                y += FloatMath.sin(theta) * distCommon
                odoY += FloatMath.sin(odoTheta) * (odoOffsetCommon)

                val realPos = RobotPose(times, 0f, x, y, theta)
                realLocs.add(realPos)
                val odoPos = RobotPose(times, 0f, odoX, odoY, odoTheta)
                odoLocs.add(odoPos)

                // make features as the robot sees them
                val features = realObjectLocs.map { getFeatureForPosition(x, y, theta, it.x, it.y, SENSOR_ANG_STD_DEV, SENSOR_DIST_STD_DEV) }

                // perform a FastSLAM timestep
                synchronized(slam) {
                    slam.addTimeStep(features, odoPos)
                }
                sendUpdateEvent(realPos, odoPos, slam.particlePoses, features, realObjectLocs)

                if (shouldPause.get() == true) {
                    println("Robot $rid attempting to pause")
                    pauseSemaphore.acquire()
                    pauseSemaphore.release()
                    println("Robot $rid unpaused")
                }

                val endTime = System.currentTimeMillis()
                val timeToSleep = MIN_TIMESTEP - (endTime - startTime)
                if (timeToSleep > 0) {
                    Thread.sleep(timeToSleep)
                }
            }
            println("...Robot $rid stopped")
        }
    }

    /**
     * Send data that is never modified after being produced, using a helper thread
     * (TODO: could hard guarantee locking not required by using immutability)
     */
    private fun sendUpdateEvent(truePos: Pose, odoPos: Pose, particlePoses: List<Pose>, featuresForRT: List<Feature>,
        realLandmarks: ArrayList<xyPnt>) {
        updateExecutor.execute {
            val rtParticlePoses = particlePoses.map {
                RTParticle(it.x, it.y, it.heading, ArrayList<RTLandmark>())
            }
            val rtFeatures = featuresForRT.map { RTFeature(it.distance, it.angle, it.stdDev) }
            val rtOdoPos = RTPose(odoPos.x, odoPos.y, odoPos.heading)
            val rtTruePos = RTPose(truePos.x, truePos.y, truePos.heading)
            val rtTrueLandmarks = realLandmarks.map { RTLandmark(it.x, it.y, 0.0f) }

            var sumX = 0.0f
            var sumY = 0.0f
            var sumHeading = 0.0f
            particlePoses.forEach {
                sumX += it.x
                sumY += it.y
                sumHeading += it.heading
            }
            val rtBestPose = RTPose(sumX / particlePoses.size, sumY / particlePoses.size, sumHeading / particlePoses.size)

            val rtMsg = RTMsg(FastSlamInfo(System.currentTimeMillis(), rtParticlePoses, rtFeatures, rtBestPose, rtOdoPos, rtTruePos, rtTrueLandmarks))
            rtUpdateEventCont(this, rtMsg)
        }
    }

    /**
     * Starts the robot if it is not already running
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
    fun pauseRobot() {
        if (shouldPause.get() == false) {
            pauseSemaphore.acquire()
            shouldPause.set(true)
        }
    }

    @Synchronized
    fun unpauseRobot() {
        if (shouldPause.get() == true) {
            shouldPause.set(false)
            pauseSemaphore.release()
        }
    }

    @Synchronized
    fun stopRobot() {
        shouldRun.set(false)
    }

    // To avoid several bugs, all public facing methods are synchronized
    @Synchronized
    fun getAvgPose(): RobotPose {
        synchronized(slam) {
            return slam.avgPose
        }
    }

    @Synchronized
    fun applyFastSlamSettings(fSettingsMsg: FastSlamSettingsMsg) {
        synchronized(slam) {
            slam.changeNumParticles(fSettingsMsg.numParticles)
            slam.changeAngleVariance(fSettingsMsg.sensorAngVar)
            slam.changeDistanceVariance(fSettingsMsg.sensorDistVar)
            rtUpdateEventCont(this, RTMsg(FastSlamSettingsMsg(slam.numParticles, slam.angleVariance, slam.distVariance)))
        }
    }

    @Synchronized
    fun applyRobotSettings(rSettings: RobotSettingsMsg) {
        if (rSettings.resetting) {
            stopRobot()
            unpauseRobot()
        }

        if (rSettings.running) {
            if (rSettings.resetting == false) {
                unpauseRobot()
            }
            startRobot()
        } else {
            pauseRobot()
        }
        rtUpdateEventCont(this, RTMsg(RobotSettingsMsg(!shouldPause.get(), !shouldRun.get())))
    }
}