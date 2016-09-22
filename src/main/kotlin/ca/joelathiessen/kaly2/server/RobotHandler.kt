package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.subconscious.sensor.SensorInfo
import ca.joelathiessen.util.EventContainer
import ca.joelathiessen.util.getFeatureForPosition
import lejos.robotics.navigation.Pose
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class RobotHandler(val rid: Long) {
    val MIN_TIMESTEP = 33//33 // Don't wait for shorter than this (in ms) before starting the next simulation timestep
    val ROT_RATE = 0.03
    val STEP_DIST = 2
    val STEP_ROT_STD_DEV = 0.01
    val STEP_DIST_STD_DEV = 0.5
    val MIN_WIDTH = 400.0
    val SENSOR_STD_DEV = 0.1

    val startPos = RobotPose(0, 0f, MIN_WIDTH.toFloat() / 2f, MIN_WIDTH.toFloat() / 2f, 0f)

    private val rtUpdateEventCont = EventContainer<RTMsg>()
    val rtUpdateEvent = rtUpdateEventCont.event

    private val updateExecutor = Executors.newSingleThreadExecutor()!!
    private var shouldRun = AtomicBoolean()
    private var simThread: Thread
    private val robotRunningLock = Any()

    private lateinit var slam: FastSLAM
    private val motionModel = CarModel()
    private val dataAssoc = NNDataAssociator()
    private val partResamp = FastUnbiasedResampler()
    private val sensorInfo = object : SensorInfo {}
    private val random = Random(1)

    private data class xyPnt(val x: Double, val y: Double)

    private val realObjectLocs = ArrayList<xyPnt>()

    init {
        for (i in 0..10) {
            realObjectLocs += xyPnt(random.nextDouble() * MIN_WIDTH, random.nextDouble() * MIN_WIDTH)
        }

        simThread = thread(start = false) {

            // for now, instead of running an entire robot, run just its FastSLAM simulation

            synchronized(robotRunningLock) {
                println("Robot $rid started...")
                slam = FastSLAM(startPos, motionModel, dataAssoc, partResamp, sensorInfo)

                var x = MIN_WIDTH / 2
                var y = MIN_WIDTH / 2
                var theta = 0.1
                var times = 0
                val odoLocs = ArrayList<RobotPose>()

                while (shouldRun.get()) {
                    val startTime = System.currentTimeMillis()

                    // move the robot
                    theta += ROT_RATE + STEP_ROT_STD_DEV * random.nextGaussian()
                    x += Math.cos(theta) * STEP_DIST + STEP_DIST_STD_DEV * random.nextGaussian()
                    y += Math.sin(theta) * STEP_DIST + STEP_DIST_STD_DEV * random.nextGaussian()
                    val realPos = RobotPose(times, 0f, x.toFloat(), y.toFloat(), theta.toFloat())
                    odoLocs.add(realPos)

                    // make features as the robot sees them
                    val features = realObjectLocs.map { getFeatureForPosition(x, y, theta, it.x, it.y, SENSOR_STD_DEV) }

                    // perform a FastSLAM timestep
                    synchronized(slam) {
                        slam.addTimeStep(features, realPos)
                    }
                    sendUpdateEvent(realPos, slam.particlePoses, features, realObjectLocs)

                    val endTime = System.currentTimeMillis()
                    val timeToSleep = MIN_TIMESTEP - (endTime - startTime)
                    if (timeToSleep > 0) {
                        Thread.sleep(timeToSleep)
                    }
                }
                println("...Robot $rid stopped")
            }
        }
    }

    /**
     * Send data that is never modified after being produced, using a helper thread
     * (TODO: could hard guarantee locking not required by using immutability)
     */
    private fun sendUpdateEvent(lastRealPos: Pose, particlePoses: List<Pose>, featuresForRT: List<Feature>,
                                realLandmarks: ArrayList<xyPnt>) {
        updateExecutor.execute {
            val rtParticlePoses = particlePoses.map {
                RTParticle(it.x, it.y, it.heading, ArrayList<RTLandmark>())
            }
            val rtFeatures = featuresForRT.map { RTFeature(it.distance, it.angle, it.stdDev) }
            val odoPose = RTPose(lastRealPos.x, lastRealPos.y, lastRealPos.heading)
            val truePose = odoPose // for now...
            val trueLandmarks = realLandmarks.map { RTLandmark(it.x, it.y, 0.0) }
            val rtMsg = RTMsg(System.currentTimeMillis(), rtParticlePoses, rtFeatures, odoPose, truePose, trueLandmarks)
            rtUpdateEventCont(this, rtMsg)
        }
    }

    /**
     * Starts the robot if it is not already running
     */
    @Synchronized
    fun startRobot() {
        if (shouldRun.get() == false || simThread.isAlive == false) {
            synchronized(robotRunningLock) { // wait until the robot is stopped
                shouldRun.set(true)
                simThread.start()
            }
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
}