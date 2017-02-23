package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.planner.linear.LinearPathSegmentRootFactory
import ca.joelathiessen.util.GenTree
import lejos.robotics.geometry.Line
import lejos.robotics.geometry.Point
import java.awt.Color
import java.awt.Graphics
import java.util.*
import java.util.concurrent.Executors
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

object RRTStarDemo {
    @JvmStatic fun main(args: Array<String>) {
        val frame = JFrame()
        var panel = RRTStarView()
        frame.add(panel)
        frame.setSize(500, 500)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setVisible(true)
    }
}

class RRTStarView : JPanel() {
    private val WINDOW_SIZE = 500
    private val HALF_SIZE = WINDOW_SIZE / 2
    private val OBS_SIZE = 1.0f
    private val SEARCH_DIST = HALF_SIZE.toFloat()
    private val STEP_DIST = 20.0f
    private val ITERATIONS = 100
    private val MAX_FRAME_TIME = 16L
    private val drawLock = Any()
    private val printExec = Executors.newFixedThreadPool(1)!!

    private var paths = ArrayList<PathSegmentInfo>()
    private var maneuvers: List<RobotPose> = ArrayList()
    private var count = 0

    init {
        this.setSize(WINDOW_SIZE, WINDOW_SIZE)

        val segmentFactory = LinearPathSegmentRootFactory()
        val planner = GlobalPathPlanner(segmentFactory, GenTree<Point>(), OBS_SIZE, SEARCH_DIST, STEP_DIST,
                RobotPose(0, 0f, 0f, 0f, 0f), RobotPose(0, 0f, WINDOW_SIZE.toFloat(), WINDOW_SIZE.toFloat(), 0f))

        thread {
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
        graphics.clearRect(0, 0, width, height)

        graphics.color = Color.blue
        var curPaths: List<PathSegmentInfo>? = null
        synchronized(drawLock) {
            curPaths = paths
        }
        
        curPaths!!.forEach {
            it.getLines().forEach {
                graphics.drawLine(it.x1.toInt() + HALF_SIZE, it.y1.toInt() + HALF_SIZE,
                        it.x2.toInt() + HALF_SIZE, it.y2.toInt() + HALF_SIZE)
            }
        }

        val manPoints = maneuvers.map { Point(it.x, it.y) }
        val manLines = ArrayList<Line>()
        for (i in 1 until manPoints.size) {
            manLines.add(Line(manPoints[i - 1].x, manPoints[i - 1].y,
                    manPoints[i].x, manPoints[i].y))
        }
        graphics.color = Color.green
        manLines.forEach {
            graphics.drawLine(it.x1.toInt() + HALF_SIZE, it.y1.toInt() + HALF_SIZE,
                    it.x2.toInt() + HALF_SIZE, it.y2.toInt() + HALF_SIZE)
        }
    }
}
