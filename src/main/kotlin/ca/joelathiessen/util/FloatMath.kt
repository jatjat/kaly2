package ca.joelathiessen.util

// Kotlin does not support static extension functions on Java classes at this point
object FloatMath {

    fun sin(value: Float) = Math.sin(value.toDouble()).toFloat()

    fun cos(value: Float) = Math.cos(value.toDouble()).toFloat()

    fun atan2(y: Float, x: Float) = Math.atan2(y.toDouble(), x.toDouble()).toFloat()

    fun sqrt(value: Float) = Math.sqrt(value.toDouble()).toFloat()

    fun pow(a: Float, b: Float) = Math.pow(a.toDouble(), b.toDouble()).toFloat()

    fun toRadians(value: Float) = Math.toRadians(value.toDouble()).toFloat()

}
