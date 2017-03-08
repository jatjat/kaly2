package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.RobotCore
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.odometry.AccurateSlamOdometry
import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.planner.linear.LinearPathSegmentRootFactory
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.subconscious.*
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2Sensor
import ca.joelathiessen.kaly2.subconscious.sensor.SimSensor
import ca.joelathiessen.kaly2.subconscious.sensor.SimSpinner
import ca.joelathiessen.kaly2.subconscious.sensor.Spinnable
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.GenTree
import ca.joelathiessen.util.array2d
import lejos.robotics.geometry.Line
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

object MainLoopDemo {
    @JvmStatic fun main(args: Array<String>) {
        val frame = JFrame()
        val panel = MainLoopView()
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
    private val STEP_DIST = 2f

    private val MIN_WIDTH = 400.0f

    private val NN_THRESHOLD = 10.0f

    private val OBS_SIZE = 2f
    private val SEARCH_DIST = MIN_WIDTH
    private val GBL_PTH_PLN_STEP_DIST = 20f
    private val GBL_PTH_PLN_ITRS = 1000

    private val LCL_PLN_ROT_STEP = 0.017f
    private val LCL_PLN_DIST_STEP = 1f
    private val LCL_PLN_GRID_STEP = 5f
    private val LCL_PLN_GRID_SIZE = 2 * MAX_SENSOR_RANGE
    private val LCL_PLN_MAX_ROT = 1f
    private val LCL_PLN_MAX_DIST = 20f

    private val MIN_MES_TIME = 160L
    private val SUBCONSC_QUEUE_SIZE = 100

    private val startPos = RobotPose(0, 0f, MIN_WIDTH / 2f, MIN_WIDTH / 2f, 0f)
    private val motionModel = CarModel()
    private val dataAssoc = NNDataAssociator(NN_THRESHOLD)
    private val partResamp = FastUnbiasedResampler()

    private val drawFeatLock = Any()
    private val drawPartLock = Any()
    private val realLock = Any()
    private val odoLock = Any()

    private val realLocs = ArrayList<RobotPose>()
    private var odoLocs: ArrayList<Pose> = ArrayList()

    private val obstacles = GenTree<Point>()
    private val obsGrid = array2d<Point?>(image.width, image.height, { null })

    private var drawParticlePoses: List<Pose> = ArrayList()
    private var drawFeatures: List<Feature> = ArrayList()

    private var drawPaths: List<PathSegmentInfo> = ArrayList<PathSegmentInfo>()
    private var drawManeuvers: List<RobotPose> = ArrayList()
    private var drawPlan = LocalPlan(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0f)

    private val drawPathLock = Any()
    private val drawManeuversLock = Any()
    private val drawPlanLock = Any()

    private val factory = LinearPathSegmentRootFactory()

    init {
        this.setSize(MIN_WIDTH.toInt(), MIN_WIDTH.toInt())

        var x = MIN_WIDTH / 2f
        var y = MIN_WIDTH / 2f
        var xEnd = MIN_WIDTH
        var yEnd = MIN_WIDTH

        val points = ArrayList<Point>()
        for (xInc in 0 until image.width) {
            for (yInc in 0 until image.height) {
                if (image.getRGB(xInc, yInc) == Color.BLACK.rgb) {
                    points.add(Point(xInc.toFloat(), yInc.toFloat()))
                    obsGrid[xInc][yInc] = Point(xInc.toFloat(), yInc.toFloat())
                } else if (image.getRGB(xInc, yInc) == Color.RED.rgb) {
                    x = xInc.toFloat()
                    y = yInc.toFloat()
                } else if (image.getRGB(xInc, yInc) == Color.GREEN.rgb) {
                    xEnd = xInc.toFloat()
                    yEnd = yInc.toFloat()
                }
            }
        }
        val startPose = RobotPose(0, 0f, x, y, 0f)
        val end = RobotPose(0, 0f, xEnd, yEnd, 0f)

        Collections.shuffle(points)
        points.forEach { obstacles.add(it.x, it.y, it) }


        val simPilot = SimulatedPilot(ODO_ANG_STD_DEV, ODO_DIST_STD_DEV, STEP_DIST, startPose)
        val simSpinner = SimSpinner(SENSOR_START_ANG, SENSOR_END_ANG, SENSOR_ANG_INCR)
        val simSensor = SimSensor(obsGrid, image.width, image.height,
                MAX_SENSOR_RANGE, SENSOR_DIST_STDEV, SENSOR_ANG_STDEV, simSpinner, simPilot)

        val robotPilot: RobotPilot = simPilot
        val sensor: Kaly2Sensor = simSensor
        val spinner: Spinnable = simSpinner

        val gblManeuvers: AtomicReference<List<RobotPose>> = AtomicReference(ArrayList())
        val resultsQueue = ArrayBlockingQueue<SubconsciousThreadedResults>(SUBCONSC_QUEUE_SIZE)
        val accurateOdo = AccurateSlamOdometry(robotPilot.poses.odoPose, { robotPilot.poses.odoPose })

        val localPlanner = LocalPlanner(0f, LCL_PLN_ROT_STEP, LCL_PLN_DIST_STEP, LCL_PLN_GRID_STEP,
                LCL_PLN_GRID_SIZE, OBS_SIZE)

        val subConsc = SubconsciousThreaded(robotPilot, accurateOdo, localPlanner, LCL_PLN_MAX_ROT, LCL_PLN_MAX_DIST,
                sensor, spinner, gblManeuvers, resultsQueue, MIN_MES_TIME)

        val slam = FastSLAM(startPos, motionModel, dataAssoc, partResamp, sensor)

        val featureDetector = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val globalPathPlannerFactory = { obstacles: GenTree<Point>, curPose: RobotPose, goalPose: RobotPose ->
            GlobalPathPlanner(factory, obstacles, OBS_SIZE, SEARCH_DIST, GBL_PTH_PLN_STEP_DIST, curPose, goalPose)
        }

        val robotCore = RobotCore(end, subConsc, accurateOdo, slam, featureDetector, globalPathPlannerFactory, GBL_PTH_PLN_ITRS, obstacles)

        thread {

            robotCore.startSubconscious()

            while (true) {
                robotCore.iterate()
                val subResults = robotCore.subconcResults
                val features = robotCore.features
                val maneuvers = robotCore.maneuvers
                val paths = robotCore.paths
                val particlePoses = robotCore.particlePoses

                synchronized(drawPathLock) {
                    drawPaths = paths
                }

                if (subResults != null) {
                    synchronized(odoLock) {
                        odoLocs.add(subResults.pilotPoses.odoPose)
                    }
                    synchronized(realLock) {
                        val simPoses = subResults.pilotPoses
                        if (simPoses is SimPilotPoses) {
                            realLocs.add(simPoses.realPose)
                        }
                    }
                    synchronized(drawPlanLock) {
                        drawPlan = subResults.plan
                    }
                }
                synchronized(drawManeuversLock) {
                    drawManeuvers = maneuvers
                }
                synchronized(drawFeatLock) {
                    drawFeatures = features
                }
                synchronized(drawPartLock) {
                    drawParticlePoses = particlePoses
                }

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

        // draw the maneuvers
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

        // draw the aim of the local plan
        graphics.color = Color.MAGENTA
        val paintDrawPlan = synchronized(drawPlanLock) { drawPlan }
        graphics.drawRect(paintDrawPlan.endX.toInt(), paintDrawPlan.endY.toInt(), 3, 3)
    }
}