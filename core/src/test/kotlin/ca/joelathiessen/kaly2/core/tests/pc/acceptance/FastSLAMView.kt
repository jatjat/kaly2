package ca.joelathiessen.kaly2.core.tests.pc.acceptance

import ca.joelathiessen.kaly2.core.odometry.CarModel
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.slam.FastSLAM
import ca.joelathiessen.kaly2.core.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.core.slam.NNDataAssociator
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.FloatRandom
import ca.joelathiessen.util.getFeatureForPosition
import java.awt.Color
import java.awt.Graphics
import java.util.ArrayList
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.concurrent.fixedRateTimer

object FastSLAMDemo {
    @JvmStatic
    fun main(args: Array<String>) {
        val frame = JFrame()
        var panel = FastSLAMView()
        frame.add(panel)
        frame.setSize(500, 500)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setVisible(true)
    }
}

class FastSLAMView : JPanel() {
    val ROT_RATE = 0.03f
    val STEP_DIST = 2f
    val STEP_ROT_STD_DEV = 0.01f
    val STEP_DIST_STD_DEV = 0.5f
    val MIN_WIDTH = 400.0f
    val DEFAULT_NUM_PARTICLES = 20
    val DEFAULT_DIST_VARIANCE = 1.0f
    val DEFAULT_ANG_VARIANCE = 0.01f
    val IDENTITY_VARIANCE = 0.2f

    val SENSOR_STD_DEV = 0.1f

    val startPos = RobotPose(0, 0f, MIN_WIDTH / 2f, MIN_WIDTH / 2f, 0f)
    val motionModel = CarModel()
    val dataAssoc = NNDataAssociator()
    val partResamp = FastUnbiasedResampler()
    var realPos: RobotPose = startPos
    val random = FloatRandom(1)

    val drawLock = Any()

    data class xyPnt(val x: Float, val y: Float)

    val realObjectLocs = ArrayList<xyPnt>()
    val odoLocs = ArrayList<RobotPose>()

    var slam = FastSLAM(startPos, motionModel, dataAssoc, partResamp, DEFAULT_NUM_PARTICLES, DEFAULT_DIST_VARIANCE,
            DEFAULT_ANG_VARIANCE, IDENTITY_VARIANCE)

    var x = MIN_WIDTH / 2
    var y = MIN_WIDTH / 2
    var theta = 0.1f
    var times = 0L

    lateinit var drawOdoLocs: List<Pair<Int, Int>>
    lateinit var drawParticlePoses: List<Pair<Int, Int>>
    var drawRealObjectLocs: List<Pair<Int, Int>>

    init {
        this.setSize(MIN_WIDTH.toInt(), MIN_WIDTH.toInt())

        for (i in 0..10) {
            realObjectLocs += xyPnt(random.nextFloat() * MIN_WIDTH, random.nextFloat() * MIN_WIDTH)
        }

        drawRealObjectLocs = realObjectLocs.map { Pair(it.x.toInt(), it.y.toInt()) }

        fixedRateTimer(period = 50) {
            mainLoop()
        }
    }

    fun mainLoop() {
        //move the robot
        theta += ROT_RATE + STEP_ROT_STD_DEV * random.nextGaussian()
        x += FloatMath.cos(theta) * STEP_DIST + STEP_DIST_STD_DEV * random.nextGaussian()
        y += FloatMath.sin(theta) * STEP_DIST + STEP_DIST_STD_DEV * random.nextGaussian()
        realPos = RobotPose(times, 0f, x, y, theta)

        //make features as the robot sees them
        val features = realObjectLocs.map { getFeatureForPosition(x, y, theta, it.x, it.y, SENSOR_STD_DEV) }
        synchronized(drawLock) {
            odoLocs.add(realPos)
            slam.addTimeStep(features, realPos)
        }
        this@FastSLAMView.repaint()
    }

    override fun paint(graphics: Graphics) {

        synchronized(drawLock) {
            drawOdoLocs = odoLocs.map { Pair(it.x.toInt(), it.y.toInt()) }
            drawParticlePoses = slam.particlePoses.map { Pair(it.x.toInt(), it.y.toInt()) }
        }

        // track the robot's real/odometric position
        graphics.color = Color.RED
        drawOdoLocs.forEach { graphics.drawRect(it.first, it.second, 2, 2) }

        // draw the particles
        graphics.color = Color.blue
        drawParticlePoses.forEach {
            graphics.drawRect(it.first, it.second, 2, 2)
        }

        // draw the objects
        graphics.color = Color.GREEN
        drawRealObjectLocs.forEach {
            graphics.drawRect(it.first, it.second, 2, 2)
        }
    }
}