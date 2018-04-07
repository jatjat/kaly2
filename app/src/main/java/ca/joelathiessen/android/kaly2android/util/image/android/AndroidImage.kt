package ca.joelathiessen.util.image.android

import ca.joelathiessen.util.image.AndroidJVMImage
import ca.joelathiessen.util.image.Color
import android.graphics.Bitmap


class AndroidImage(private val image: Bitmap) : AndroidJVMImage {
    override val width: Int
        get() = image.width

    override val height: Int
        get() = image.height

    override fun getColor(x: Int, y: Int): Color {
        return when(image.getPixel(x, y)) {
            android.graphics.Color.RED -> Color.RED
            android.graphics.Color.GREEN -> Color.GREEN
            android.graphics.Color.BLUE -> Color.BLUE
            android.graphics.Color.BLACK -> Color.BLACK
            android.graphics.Color.WHITE -> Color.WHITE
            else -> Color.OTHER
        }
    }

}