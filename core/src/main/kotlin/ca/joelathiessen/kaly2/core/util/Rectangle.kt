package ca.joelathiessen.util

data class Rectangle(val x: Double, val y: Double, val width: Double, val height: Double) {
    val x2: Double by lazy { x + width }
    val y2: Double by lazy { y + height }

    fun contains(xIn: Double, yIn: Double): Boolean {
        return xIn >= x && xIn <= x2 && yIn >= y && yIn <= y2
    }
}