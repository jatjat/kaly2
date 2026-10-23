package ca.joelathiessen.kaly2.core.tests.pc.acceptance

import ca.joelathiessen.kaly2.core.map.MapTree
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.core.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.core.planner.linear.LinearPathSegmentRootFactory
import lejos.robotics.geometry.Line
import lejos.robotics.geometry.Point
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

object RRTStarDemo {
    @JvmStatic
    fun main(args: Array<String>) {
        val frame = JFrame()
        var panel = RRTStarView()
        frame.add(panel)
        frame.setSize(500, 500)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setVisible(true)
    }
}

class RRTStarView : JPanel() {
    private val IMAGE_LOC = "/images/squareFIlledMazed.png"
    private val image = ImageIO.read(javaClass.getResource(IMAGE_LOC))

    private val WINDOW_SIZE = 500
    private val HALF_SIZE = WINDOW_SIZE / 2
    private val OBS_SIZE = 1f
    private val SEARCH_DIST = HALF_SIZE.toFloat()
    private val STEP_DIST = 20.0f
    private val ITERATIONS = 1
    private val MAX_FRAME_TIME = 16L
    private val drawLock = Any()
    private val printExec = Executors.newFixedThreadPool(1)!!

    private var paths = ArrayList<PathSegmentInfo>()
    private var maneuvers: List<RobotPose> = ArrayList()
    private var count = 0

    init {
        this.setSize(WINDOW_SIZE, WINDOW_SIZE)

        val obstacles = MapTree()

        var xStart = HALF_SIZE.toFloat()
        var yStart = HALF_SIZE.toFloat()
        var xEnd = WINDOW_SIZE.toFloat()
        var yEnd = WINDOW_SIZE.toFloat()
        val points = ArrayList<Point>()
        for (xInc in 0 until image.width) {
            for (yInc in 0 until image.height) {
                if (image.getRGB(xInc, yInc) == Color.BLACK.rgb) {
                    points.add(Point(xInc.toFloat(), yInc.toFloat()))
                } else if (image.getRGB(xInc, yInc) == Color.RED.rgb) {
                    xStart = xInc.toFloat()
                    yStart = yInc.toFloat()
                } else if (image.getRGB(xInc, yInc) == Color.GREEN.rgb) {
                    xEnd = xInc.toFloat()
                    yEnd = yInc.toFloat()
                }
            }
        }
        Collections.shuffle(points)
        points.forEach { obstacles.add(it) }

        val segmentFactory = LinearPathSegmentRootFactory()
        val planner = GlobalPathPlanner(segmentFactory, obstacles, OBS_SIZE, SEARCH_DIST, STEP_DIST,
            RobotPose(0, 0f, xStart, yStart, 0f), RobotPose(0, 0f, xEnd, yEnd, 0f))

        thread(name = "DrawPathsManeuvers") {
            while (true) {
                planner.iterate(ITERATIONS)
                synchronized(drawLock) {
                    paths = planner.paths
                    maneuvers = planner.getManeuvers()
                }
                count += ITERATIONS
                printExec.run { println(count) }
            }
        }

        fixedRateTimer(period = MAX_FRAME_TIME) {
            this@RRTStarView.repaint()
        }
    }

    override fun paint(graphics: Graphics) {
        (graphics as Graphics2D).drawImage(image, 0, 0, null)

        graphics.color = Color.blue
        var curPaths: List<PathSegmentInfo>? = null
        synchronized(drawLock) {
            curPaths = paths
        }

        curPaths!!.forEach {
            it.getLines().forEach {
                graphics.drawLine(it.x1.toInt(), it.y1.toInt(), it.x2.toInt(), it.y2.toInt())
            }
        }

        val manPoints = maneuvers.map { Point(it.x, it.y) }
        val manLines = ArrayList<Line>()
        for (i in 1 until manPoints.size) {
            manLines.add(Line(manPoints[i - 1].x, manPoints[i - 1].y, manPoints[i].x, manPoints[i].y))
        }
        graphics.color = Color.green
        manLines.forEach {
            graphics.drawLine(it.x1.toInt(), it.y1.toInt(), it.x2.toInt(), it.y2.toInt())
        }
    }
}
