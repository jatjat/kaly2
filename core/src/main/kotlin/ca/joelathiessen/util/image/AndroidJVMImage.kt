package ca.joelathiessen.util.image

interface AndroidJVMImage {
    val width: Int
    val height: Int
    fun getColor(x: Int, y: Int): Color
}