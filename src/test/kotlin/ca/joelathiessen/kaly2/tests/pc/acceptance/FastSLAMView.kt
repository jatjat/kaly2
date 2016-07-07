package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.subconscious.sensor.SensorInfo
import ca.joelathiessen.kaly2.tests.pc.unit.slam.getFeatureForPosition
import org.mockito.Mockito
import java.awt.Color
import java.awt.Graphics
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.concurrent.fixedRateTimer

object FastSLAMDemo {
    @JvmStatic fun main(args: Array<String>) {
        val frame = JFrame()
        var panel = FastSLAMView()
        frame.add(panel)
        frame.setSize(500, 500)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setVisible(true)
    }
}

class FastSLAMView : JPanel() {
    val ROT_RATE = 0.03
    val STEP_DIST = 2
    val STEP_ROT_STD_DEV = 0.01
    val STEP_DIST_STD_DEV = 0.5
    val MIN_WIDTH = 400.0

    val SENSOR_STD_DEV = 0.1

    val startPos = RobotPose(0, 0f, MIN_WIDTH.toFloat() / 2f, MIN_WIDTH.toFloat() / 2f, 0f)
    val motionModel = CarModel()
    val dataAssoc = NNDataAssociator()
    val partResamp = FastUnbiasedResampler()
    val sensorInfo = Mockito.mock(SensorInfo::class.java)
    var realPos: RobotPose = startPos
    val random = Random(1)

    val drawLock = Any()

    data class xyPnt(val x: Double, val y: Double)

    val realObjectLocs = ArrayList<xyPnt>()
    val odoLocs = ArrayList<RobotPose>()

    var slam = FastSLAM(startPos, motionModel, dataAssoc, partResamp, sensorInfo)

    var x = MIN_WIDTH / 2
    var y = MIN_WIDTH / 2
    var theta = 0.1
    var times = 0

    var drawOdoLocs: List<Pair<Int, Int>> = ArrayList()
    var drawParticlePoses: List<Pair<Int, Int>> = ArrayList()
    val drawRealObjectLocs = realObjectLocs.map { Pair(it.x.toInt(), it.y.toInt()) }

    init {
        this.setSize(MIN_WIDTH.toInt(), MIN_WIDTH.toInt())

        for (i in 0..10) {
            realObjectLocs += xyPnt(random.nextDouble() * MIN_WIDTH, random.nextDouble() * MIN_WIDTH)
        }

        fixedRateTimer(period = 50) {
            mainLoop()
        }
    }

    fun mainLoop() {
        //move the robot
        theta += ROT_RATE + STEP_ROT_STD_DEV * random.nextGaussian()
        x += Math.cos(theta) * STEP_DIST + STEP_DIST_STD_DEV * random.nextGaussian()
        y += Math.sin(theta) * STEP_DIST + STEP_DIST_STD_DEV * random.nextGaussian()
        realPos = RobotPose(times, 0f, x.toFloat(), y.toFloat(), theta.toFloat())

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