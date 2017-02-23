package ca.joelathiessen.util

// Kotlin does not support static extension functions on Java classes at this point
object FloatMath {
    val PI = Math.PI.toFloat()

    fun sin(value: Float): Float = Math.sin(value.toDouble()).toFloat()

    fun cos(value: Float): Float = Math.cos(value.toDouble()).toFloat()

    fun atan2(y: Float, x: Float): Float = Math.atan2(y.toDouble(), x.toDouble()).toFloat()

    fun sqrt(value: Float): Float = Math.sqrt(value.toDouble()).toFloat()

    fun pow(a: Float, b: Float): Float = Math.pow(a.toDouble(), b.toDouble()).toFloat()

    fun toRadians(value: Float): Float = Math.toRadians(value.toDouble()).toFloat()
    fun toRadians(value: Int): Float = Math.toRadians(value.toDouble()).toFloat()

    fun abs(value: Float): Float = Math.abs(value)

    fun max(a: Float, b: Float): Float = Math.max(a, b)

    fun log(value: Float): Float = Math.log(value.toDouble()).toFloat()

    fun exp(value: Float): Float = Math.exp(value.toDouble()).toFloat()
}
