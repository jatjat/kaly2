package ca.joelathiessen.util.image.android

import ca.joelathiessen.util.image.AndroidJVMImage
import ca.joelathiessen.util.image.Color
import android.graphics.Bitmap


class AndroidImage(private val image: Bitmap, val COLOR_EPSILON: Int = 50) : AndroidJVMImage {

    override val width: Int
        get() = image.width

    override val height: Int
        get() = image.height

    override fun getColor(x: Int, y: Int): Color {
        val pix = image.getPixel(x, y)
        return when {
            colorMatches(android.graphics.Color.RED, pix, COLOR_EPSILON) -> Color.RED
            colorMatches(android.graphics.Color.GREEN, pix, COLOR_EPSILON) -> Color.GREEN
            colorMatches(android.graphics.Color.BLUE, pix, COLOR_EPSILON) -> Color.BLUE
            colorMatches(android.graphics.Color.BLACK, pix, COLOR_EPSILON) -> Color.BLACK
            colorMatches(android.graphics.Color.WHITE, pix, COLOR_EPSILON) -> Color.WHITE
            else -> Color.OTHER
        }
    }

    fun colorMatches(androidColor: Int, pixelColor: Int, epsilon: Int): Boolean {
        val aRed = android.graphics.Color.red(androidColor)
        val aGreen = android.graphics.Color.green(androidColor)
        val aBlue = android.graphics.Color.blue(androidColor)

        val pRed = android.graphics.Color.red(pixelColor)
        val pGreen = android.graphics.Color.green(pixelColor)
        val pBlue = android.graphics.Color.blue(pixelColor)

        return (Math.abs(aRed - pRed) + Math.abs(aGreen - pGreen) + Math.abs(aBlue - pBlue)) < epsilon
    }
}
