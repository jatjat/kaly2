package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.RobotCoreActedResults
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.server.KalyServer
import ca.joelathiessen.kaly2.server.messages.RTMsg
import ca.joelathiessen.kaly2.subconscious.LocalPlan
import ca.joelathiessen.kaly2.subconscious.SimPilotPoses
import lejos.robotics.geometry.Line
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.util.ArrayList
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

object MainLoopDemo {
    @JvmStatic
    fun main(args: Array<String>) {
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

    private val MIN_WIDTH = 400.0f

    private val drawFeatLock = Any()
    private val drawPartLock = Any()
    private val realLock = Any()
    private val odoLock = Any()

    private val realLocs = ArrayList<RobotPose>()
    private var odoLocs: ArrayList<Pose> = ArrayList()

    private var drawParticlePoses: List<Pose> = ArrayList()
    private var drawFeatures: List<Feature> = ArrayList()

    private var drawPaths: List<PathSegmentInfo> = ArrayList<PathSegmentInfo>()
    private var drawManeuvers: List<RobotPose> = ArrayList()
    private var drawPlan = LocalPlan(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0f)
    private var drawObstacles: List<Point> = ArrayList()

    private val drawPathLock = Any()
    private val drawManeuversLock = Any()
    private val drawPlanLock = Any()
    private val drawObsLock = Any()

    private val handleRTMessageCaller = { sender: Any, msg: RTMsg -> handleRTMessage(sender, msg) } // can't pass handleRTMessage directly

    init {
        this.setSize(MIN_WIDTH.toInt(), MIN_WIDTH.toInt())

        val server = KalyServer()
        thread {
            server.serve()
        }

        val robotSession = server.inprocessAPI.getRobotSession(0)
        robotSession.subscribeToRTEvents(handleRTMessageCaller)
        robotSession.startRobot()

        fixedRateTimer(period = REFRESH_INTERVAL) {
            this@MainLoopView.repaint()
        }
    }

    fun handleRTMessage(@Suppress("UNUSED_PARAMETER") sender: Any, message: RTMsg) {
        if (message.msg is RobotCoreActedResults) {
            val results = message.msg as RobotCoreActedResults
            synchronized(drawPathLock) {
                drawPaths = results.globalPlannerPaths
            }
            val subResults = results.subconcResults
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

            synchronized(drawManeuversLock) {
                drawManeuvers = results.maneuvers
            }
            synchronized(drawFeatLock) {
                drawFeatures = results.features
            }
            synchronized(drawPartLock) {
                drawParticlePoses = results.particlePoses
            }
            synchronized(drawObsLock) {
                drawObstacles = results.obstacles
            }
        }
    }

    override fun paint(graphics: Graphics) {
        val graphics2d = graphics as Graphics2D
        graphics2d.drawImage(image, 0, 0, null)

        // draw the obstacles
        graphics.color = Color.ORANGE
        synchronized(drawObsLock) {
            drawObstacles.forEach {
                graphics.drawRect(it.x.toInt(), it.y.toInt(), 1, 1)
            }
        }

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