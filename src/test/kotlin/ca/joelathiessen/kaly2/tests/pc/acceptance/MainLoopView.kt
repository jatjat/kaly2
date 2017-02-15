package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.subconscious.sensor.SimSensor
import ca.joelathiessen.util.array2d
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

object MainLoopDemo {
    @JvmStatic fun main(args: Array<String>) {
        val frame = JFrame()
        var panel = MainLoopView()
        frame.add(panel)
        frame.setSize(500, 500)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setVisible(true)
    }
}

class MainLoopView : JPanel() {
    private val IMAGE_LOC = "/images/squareFIlled.png"
    private val image = ImageIO.read(javaClass.getResource(IMAGE_LOC))

    private val REFRESH_INTERVAL = 16L

    private val LINE_THRESHOLD = 10.0
    private val CHECK_WITHIN_ANGLE = 0.3
    private val MAX_RATIO = 1.0

    private val MAX_SENSOR_RANGE = 500.0
    private val SENSOR_DIST_STDEV = 0.01
    private val SENSOR_ANG_STDEV = 0.001
    private val SENSOR_START_ANG = 0.0
    private val SENSOR_END_ANG = 2 * Math.PI
    private val SENSOR_ANG_INCR = 0.0174533

    private val ODO_ANG_STD_DEV = 0.01
    private val ODO_DIST_STD_DEV = 0.01

    private val ROT_RATE = 0.035
    private val STEP_DIST = 2
    private val STEP_ROT_STD_DEV = 0.01
    private val STEP_DIST_STD_DEV = 0.5
    private val MIN_WIDTH = 400.0

    private val startPos = RobotPose(0, 0f, MIN_WIDTH.toFloat() / 2f, MIN_WIDTH.toFloat() / 2f, 0f)
    private val motionModel = CarModel()
    private val dataAssoc = NNDataAssociator()
    private val partResamp = FastUnbiasedResampler()
    private var realPos: RobotPose = startPos
    private val random = Random(1)

    private val drawFeatLock = Any()
    private val drawPartLock = Any()
    private val realLock = Any()
    private val odoLock = Any()

    private val realLocs = ArrayList<RobotPose>()
    private var odoLocs: ArrayList<Pose> = ArrayList()

    private val obsGrid = array2d<Point?>(image.width, image.height, { null })
    private val sensor = SimSensor(realPos, SENSOR_START_ANG, obsGrid, image.width, image.height,
            MAX_SENSOR_RANGE, SENSOR_DIST_STDEV, SENSOR_ANG_STDEV)
    private val slam = FastSLAM(startPos, motionModel, dataAssoc, partResamp, sensor)

    private var drawParticlePoses: List<Pose> = ArrayList()
    private var drawFeatures: List<Feature> = ArrayList()

    init {
        this.setSize(MIN_WIDTH.toInt(), MIN_WIDTH.toInt())

        thread {
            var x = MIN_WIDTH / 2
            var y = MIN_WIDTH / 2
            var theta = 0.1
            var times = 0

            for (xInc in 0 until image.width) {
                for (yInc in 0 until image.height) {
                    if (image.getRGB(xInc, yInc).equals(Color.BLACK.rgb)) {
                        obsGrid[xInc][yInc] = Point(xInc.toFloat(), yInc.toFloat())
                    } else if (image.getRGB(xInc, yInc).equals(Color.RED.rgb)) {
                        x = xInc.toDouble()
                        y = yInc.toDouble()
                    }
                }
            }

            var odoX = x
            var odoY = y
            var odoTheta = theta

            while (true) {
                // move the robot
                val dTheta = ROT_RATE + (STEP_ROT_STD_DEV * random.nextGaussian())
                theta += dTheta
                val dOdoTheta = dTheta + ODO_ANG_STD_DEV * random.nextGaussian()
                odoTheta += dOdoTheta

                val distCommon = STEP_DIST + (STEP_DIST_STD_DEV * random.nextGaussian())
                val odoDist = distCommon + ODO_DIST_STD_DEV * random.nextGaussian()
                x += (Math.cos(theta) * distCommon)
                odoX += Math.cos(odoTheta) * odoDist

                y += Math.sin(theta) * distCommon
                odoY += Math.sin(odoTheta) * odoDist

                val realPos = RobotPose(times, 0f, x.toFloat(), y.toFloat(), theta.toFloat())
                synchronized(realLock) { realLocs.add(realPos) }
                val odoPos = RobotPose(times, 0f, odoX.toFloat(), odoY.toFloat(), odoTheta.toFloat())
                synchronized(odoLock) { odoLocs.add(odoPos) }

                // get measurements as the robot sees them
                val measurements = ArrayList<Measurement>()
                sensor.robotPose = realPos
                sensor.sensorAng = SENSOR_START_ANG

                val avgPose = slam.avgPose
                val mesX = avgPose.x + (Math.cos(avgPose.heading + dOdoTheta) * odoDist)
                val mesY = avgPose.y + (Math.sin(avgPose.heading + dOdoTheta) * odoDist)
                val mesTheta = avgPose.heading + dOdoTheta
                val mesPose = RobotPose(times, 0f, mesX.toFloat(), mesY.toFloat(), mesTheta.toFloat())

                while (sensor.sensorAng < SENSOR_END_ANG) {
                    var sample = FloatArray(2)
                    sensor.fetchSample(sample, 0)
                    measurements.add(Measurement(sample[0].toDouble(), sample[1].toDouble(), mesPose, System.nanoTime()))

                    // move the sensor
                    sensor.sensorAng += SENSOR_ANG_INCR
                }

                // make features
                val featureDetector = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)
                val features = featureDetector.getFeatures(measurements)

                slam.addTimeStep(features, odoPos)

                synchronized(drawFeatLock) {
                    drawFeatures = features
                }
                val particlePoses = slam.particlePoses
                synchronized(drawPartLock) {
                    drawParticlePoses = particlePoses
                }

                times++
            }
        }

        fixedRateTimer(period = REFRESH_INTERVAL) {
            this@MainLoopView.repaint()
        }
    }

    override fun paint(graphics: Graphics) {
        val graphics2d = graphics as Graphics2D
        graphics2d.drawImage(image, 0, 0, null)

        // track the robot's odometric position
        graphics.color = Color.RED
        synchronized(odoLock) {
            odoLocs.forEach { graphics.drawRect(it.x.toInt(), it.y.toInt(), 1, 1) }
        }

        // track the robot's real position
        graphics.color = Color.DARK_GRAY
        synchronized(realLock) {
            realLocs.forEach { graphics.drawRect(it.x.toInt(), it.y.toInt(), 1, 1) }
        }

        // draw the features
        val paintDrawFeatures = synchronized(drawFeatLock) { drawFeatures }
        paintDrawFeatures.forEach {
            graphics.color = Color.RED
            graphics.drawRect(it.x.toInt(), it.y.toInt(), 3, 3)
        }

        // draw the particles
        graphics.color = Color.BLUE
        val paintDrawParticlePoses = synchronized(drawPartLock) { drawParticlePoses }
        paintDrawParticlePoses.forEach {
            graphics.drawRect(it.x.toInt(), it.y.toInt(), 2, 2)
        }
    }
}
