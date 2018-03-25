package ca.joelathiessen.util.image.jvm

import ca.joelathiessen.util.image.AndroidJVMImage
import ca.joelathiessen.util.image.Color
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class JVMImage(filePath: String): AndroidJVMImage {
    private var image: BufferedImage?

    init {
        try { image = ImageIO.read(javaClass.getResource(filePath)) } catch (e: Exception) {
        }
        image = null
    }

    override val width: Int
        get() = image!!.width
    override val height: Int
        get() = image!!.width

    override fun getColor(x: Int, y: Int): Color {
        return when(image!!.getRGB(x, y)) {
            java.awt.Color.RED.rgb -> Color.RED
            java.awt.Color.GREEN.rgb -> Color.GREEN
            java.awt.Color.BLUE.rgb -> Color.BLUE
            java.awt.Color.WHITE.rgb -> Color.WHITE
            java.awt.Color.BLACK.rgb -> Color.BLACK
            else -> Color.OTHER
        }
    }
}