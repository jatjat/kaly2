package ca.joelathiessen.util

val EPSILON = 0.000001f
fun equals(a: Float, b: Float): Boolean {
    return within(a, b, EPSILON)
}

private fun within(a: Float, b: Float, epsilon: Float): Boolean {
    if (a == b) return true
    return FloatMath.abs(a - b) < epsilon * FloatMath.max(FloatMath.abs(a), FloatMath.abs(b))
}

inline fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    return FloatMath.sqrt((dx * dx) + (dy * dy))
}

inline fun <reified INNER> array2d(sizeOuter: Int, sizeInner: Int, noinline innerInit: (Int)->INNER): Array<Array<INNER>>
        = Array(sizeOuter) { Array<INNER>(sizeInner, innerInit) }