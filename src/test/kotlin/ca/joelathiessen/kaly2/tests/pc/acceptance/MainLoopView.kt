package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.planner.linear.LinearPathSegmentRootFactory
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.subconscious.sensor.SimSensor
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.FloatRandom
import ca.joelathiessen.util.GenTree
import ca.joelathiessen.util.array2d
import lejos.robotics.geometry.Line
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

    private val LINE_THRESHOLD = 10.0f
    private val CHECK_WITHIN_ANGLE = 0.3f
    private val MAX_RATIO = 1.0f

    private val MAX_SENSOR_RANGE = 500.0f
    private val SENSOR_DIST_STDEV = 0.01f
    private val SENSOR_ANG_STDEV = 0.001f
    private val SENSOR_START_ANG = 0.0f
    private val SENSOR_END_ANG = 2 * FloatMath.PI
    private val SENSOR_ANG_INCR = 0.0174533f

    private val ODO_ANG_STD_DEV = 0.01f
    private val ODO_DIST_STD_DEV = 0.01f

    private val ROT_RATE = 0.035f
    private val STEP_DIST = 2f
    private val STEP_ROT_STD_DEV = 0.01f
    private val STEP_DIST_STD_DEV = 0.5f
    private val MIN_WIDTH = 400.0f

    private val NN_THRESHOLD = 10.0f

    private val startPos = RobotPose(0, 0f, MIN_WIDTH / 2f, MIN_WIDTH / 2f, 0f)
    private val motionModel = CarModel()
    private val dataAssoc = NNDataAssociator(NN_THRESHOLD)
    private val partResamp = FastUnbiasedResampler()
    private var realPos: RobotPose = startPos
    private val random = FloatRandom(1)

    private val drawFeatLock = Any()
    private val drawPartLock = Any()
    private val realLock = Any()
    private val odoLock = Any()

    private val realLocs = ArrayList<RobotPose>()
    private var odoLocs: ArrayList<Pose> = ArrayList()

    private val obstacles = GenTree<Point>()
    private val obsGrid = array2d<Point?>(image.width, image.height, { null })
    private val sensor = SimSensor(realPos, SENSOR_START_ANG, obsGrid, image.width, image.height,
            MAX_SENSOR_RANGE, SENSOR_DIST_STDEV, SENSOR_ANG_STDEV)
    private val slam = FastSLAM(startPos, motionModel, dataAssoc, partResamp, sensor)

    private var drawParticlePoses: List<Pose> = ArrayList()
    private var drawFeatures: List<Feature> = ArrayList()

    private var drawPaths = ArrayList<PathSegmentInfo>()
    private var drawManeuvers: List<RobotPose> = ArrayList()

    private val drawPathLock = Any()
    private val drawManeuversLock = Any()

    private val factory = LinearPathSegmentRootFactory()

    private val OBS_SIZE = 2f
    private val SEARCH_DIST = MIN_WIDTH
    private val GBL_PTH_PLN_STEP_DIST = 20f
    private val GBL_PTH_PLN_ITRS = 1000

    init {
        this.setSize(MIN_WIDTH.toInt(), MIN_WIDTH.toInt())

        thread {
            var x = MIN_WIDTH / 2f
            var y = MIN_WIDTH / 2f
            var xEnd = MIN_WIDTH
            var yEnd = MIN_WIDTH
            var theta = 0.1f
            var times = 0

            val points = ArrayList<Point>()
            for (xInc in 0 until image.width) {
                for (yInc in 0 until image.height) {
                    if (image.getRGB(xInc, yInc).equals(Color.BLACK.rgb)) {
                        points.add(Point(xInc.toFloat(), yInc.toFloat()))
                        obsGrid[xInc][yInc] = Point(xInc.toFloat(), yInc.toFloat())
                    } else if (image.getRGB(xInc, yInc).equals(Color.RED.rgb)) {
                        x = xInc.toFloat()
                        y = yInc.toFloat()
                    } else if (image.getRGB(xInc, yInc) == Color.GREEN.rgb) {
                        xEnd = xInc.toFloat()
                        yEnd = yInc.toFloat()
                    }
                }
            }
            Collections.shuffle(points)
            points.forEach { obstacles.add(it.x, it.y, it) }
            val end = RobotPose(0, 0f, xEnd, yEnd, 0f)

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
                x += (FloatMath.cos(theta) * distCommon)
                odoX += FloatMath.cos(odoTheta) * odoDist

                y += FloatMath.sin(theta) * distCommon
                odoY += FloatMath.sin(odoTheta) * odoDist

                val realPos = RobotPose(times, 0f, x, y, theta)
                synchronized(realLock) { realLocs.add(realPos) }
                val odoPos = RobotPose(times, 0f, odoX, odoY, odoTheta)
                synchronized(odoLock) { odoLocs.add(odoPos) }

                // get measurements as the robot sees them
                val measurements = ArrayList<Measurement>()
                sensor.robotPose = realPos
                sensor.sensorAng = SENSOR_START_ANG

                val avgPose = slam.avgPose
                val mesX = avgPose.x + (FloatMath.cos(avgPose.heading + dOdoTheta) * odoDist)
                val mesY = avgPose.y + (FloatMath.sin(avgPose.heading + dOdoTheta) * odoDist)
                val mesTheta = avgPose.heading + dOdoTheta
                val mesPose = RobotPose(times, 0f, mesX, mesY, mesTheta)

                while (sensor.sensorAng < SENSOR_END_ANG) {
                    var sample = FloatArray(2)
                    sensor.fetchSample(sample, 0)
                    measurements.add(Measurement(sample[0], sample[1], mesPose, System.nanoTime()))

                    // move the sensor
                    sensor.sensorAng += SENSOR_ANG_INCR
                }

                // make features
                val featureDetector = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)
                val features = featureDetector.getFeatures(measurements)

                slam.addTimeStep(features, odoPos)

                val glbPthPln = GlobalPathPlanner(factory, obstacles, OBS_SIZE, SEARCH_DIST, GBL_PTH_PLN_STEP_DIST,
                        slam.avgPose, end)
                glbPthPln.iterate(GBL_PTH_PLN_ITRS)

                synchronized(drawPathLock) {
                    drawPaths = glbPthPln.paths
                }
                synchronized(drawManeuversLock) {
                    drawManeuvers = glbPthPln.getManeuvers()
                }

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

        // draw the drawPaths
        graphics.color = Color.LIGHT_GRAY
        val paintDrawPaths = synchronized(drawPathLock) { drawPaths }
        paintDrawPaths.forEach {
            it.getLines().forEach {
                graphics.drawLine(it.x1.toInt(), it.y1.toInt(), it.x2.toInt(), it.y2.toInt())
            }
        }

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

        val paintDrawManeuvers = synchronized(drawManeuversLock) { drawManeuvers }
        val manPoints = paintDrawManeuvers.map { Point(it.x, it.y) }
        val manLines = ArrayList<Line>(paintDrawManeuvers.size)
        for (i in 1 until manPoints.size) {
            manLines.add(Line(manPoints[i - 1].x, manPoints[i - 1].y,
                    manPoints[i].x, manPoints[i].y))
        }
        graphics.color = Color.GREEN
        manLines.forEach {
            graphics.drawLine(it.x1.toInt(), it.y1.toInt(), it.x2.toInt(), it.y2.toInt())
        }
    }
}