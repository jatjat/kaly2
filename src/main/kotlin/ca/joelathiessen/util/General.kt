package ca.joelathiessen.util

val DEC_FOR_FLT_PREC = 8
val DEC_FOR_FLT_SCALE = 5

val EPSILON = 0.000001f
fun equals(a: Float, b: Float): Boolean {
    return within(a, b, EPSILON)
}

fun within(a: Float, b: Float, epsilon: Float = EPSILON): Boolean {
    if (a == b) return true
    return FloatMath.abs(a - b) < epsilon * FloatMath.max(FloatMath.abs(a), FloatMath.abs(b))
}

fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    return FloatMath.sqrt((dx * dx) + (dy * dy))
}

inline fun <reified INNER> array2d(sizeOuter: Int, sizeInner: Int, noinline innerInit: (Int) -> INNER): Array<Array<INNER>>
    = Array(sizeOuter) { Array<INNER>(sizeInner, innerInit) }

data class RotateResult(var deltaX: Float, var deltaY: Float)

fun rotate(turnAngle: Float, distance: Float, heading: Float): RotateResult {
    val radius = distance / turnAngle
    var deltaY = 0.0f
    var deltaX = 0.0f
    if (FloatMath.abs(turnAngle) > EPSILON) {
        deltaY = radius * (FloatMath.cos(heading) - FloatMath.cos(heading + turnAngle))
        deltaX = radius * (FloatMath.sin(heading + turnAngle) - FloatMath.sin(heading))
    } else if (FloatMath.abs(distance) > EPSILON) {
        deltaX = distance * FloatMath.cos(heading)
        deltaY = distance * FloatMath.sin(heading)
    }
    return RotateResult(deltaX, deltaY)
}