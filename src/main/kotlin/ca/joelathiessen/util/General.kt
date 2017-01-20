package ca.joelathiessen.util

val EPSILON = 0.000001
fun equals(a: Double, b: Double): Boolean {
    return within(a, b, EPSILON)
}

private fun within(a: Double, b: Double, epsilon: Double): Boolean {
    if (a == b) return true
    return Math.abs(a - b) < epsilon * Math.max(Math.abs(a), Math.abs(b))
}

inline fun distance(x1: Double, x2: Double, y1: Double, y2: Double): Double {
    val dx = x2 - x1
    val dy = y2 - y1
    return Math.sqrt((dx * dx) + (dy * dy))
}

inline fun <reified INNER> array2d(sizeOuter: Int, sizeInner: Int, noinline innerInit: (Int)->INNER): Array<Array<INNER>>
        = Array(sizeOuter) { Array<INNER>(sizeInner, innerInit) }