package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.featuredetector.SplitAndMergeFeature
import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import javax.swing.*


object FeatureDetectorDemo {
    val IMAGE_LOC = "/images/squareFIlledRound.png"

    @JvmStatic fun main(args: Array<String>) {
        val image = ImageIO.read(javaClass.getResource(IMAGE_LOC))
        val frame = JFrame()
        var panel = FeatureDetectorView(image)
        val imageLabel = JLabel(ImageIcon(image))
        panel.add(imageLabel)
        frame.add(panel)
        frame.setSize(500, 500)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setVisible(true)
    }
}

class FeatureDetectorView(val image: BufferedImage) : JPanel() {
    private val WINDOW_SIZE = 500
    private val drawLock = Any()
    private val printExec = Executors.newFixedThreadPool(1)!!

    private val ANG_STEP = 0.0174533f // ray-cast along steps of this angle
    private val DIST_STEP = 0.5f // check for hits on steps of this distance along the ray

    private val LINE_THRESHOLD = 2.0f
    private val CHECK_WITHIN_ANGLE = 0.3f
    private val MAX_RATIO = 1.0f

    private var drawFeatures: List<Feature> = ArrayList()

    init {
        this.setSize(WINDOW_SIZE, WINDOW_SIZE)

        var sensorX = 0
        var sensorY = 0
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                if (image.getRGB(x, y) == Color.RED.rgb) {
                    sensorX = x
                    sensorY = y
                }
            }
        }

        // Gather measurements by ray-casting from the sensor location to obstacles
        val sensorLoc = Pose(sensorX.toFloat(), sensorY.toFloat(), 0f)
        val measurements = ArrayList<Measurement>()
        var ang = 0.0f
        while (ang < 2 * FloatMath.PI) {
            var cont = true
            var dist = 0.0f
            while (cont) {
                val checkX = sensorX + (FloatMath.cos(ang) * dist).toInt()
                val checkY = sensorY + (FloatMath.sin(ang) * dist).toInt()

                if (checkX >= 0 && checkX < image.width && checkY >= 0 && checkY < image.height) {
                    if (image.getRGB(checkX, checkY).equals(Color.BLACK.rgb)) {
                        measurements.add(Measurement(dist, ang, sensorLoc, 0L))
                        cont = false
                    }
                } else {
                    cont = false
                }
                dist += DIST_STEP
            }
            ang += ANG_STEP
        }

        val detector = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)
        val features = detector.getFeatures(measurements)
        features.forEachIndexed { i, feature ->
            val smFeat = feature as SplitAndMergeFeature
            printExec.execute {
                println("$i: ${smFeat.discardedPoints}")
            }
        }
        synchronized(drawLock) {
            drawFeatures = features
        }
        this@FeatureDetectorView.repaint()
    }

    override fun paint(graphics: Graphics) {
        val graphics2d = graphics as Graphics2D
        graphics2d.setStroke(BasicStroke(5f))
        graphics2d.drawImage(image, 0, 0, null)
        synchronized(drawLock) {
            drawFeatures.forEach {
                val featSM = it as SplitAndMergeFeature
                graphics.color = Color.BLUE
                graphics.drawLine(it.x.toInt(), it.y.toInt(), it.x.toInt(), it.y.toInt())
                graphics.color = Color.RED
                graphics.drawString("${featSM.discardedPoints}", it.x.toInt(), it.y.toInt())
            }
        }
    }
}