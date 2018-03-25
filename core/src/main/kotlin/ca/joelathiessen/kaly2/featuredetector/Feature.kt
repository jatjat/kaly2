package ca.joelathiessen.kaly2.featuredetector

import Jama.Matrix
import ca.joelathiessen.util.FloatMath

open class Feature(val sensorX: Float, val sensorY: Float, val distance: Float, val angle: Float,
    val deltaX: Float = FloatMath.cos(angle) * distance, val deltaY: Float = FloatMath.sin(angle) * distance,
    val stdDev: Float) {

    val x = deltaX + sensorX
    val y = deltaY + sensorY

    fun makeJacobian(): Matrix {
        val distanceAbs = FloatMath.abs(distance)
        val distSq = distanceAbs * distanceAbs
        val deltaX = x - sensorX
        val deltaY = y - sensorY
        return Matrix(arrayOf(
            doubleArrayOf(-1.0 * deltaX / distanceAbs, -1.0 * deltaY / distanceAbs, 0.0),
            doubleArrayOf((deltaY / distSq).toDouble(), -1.0 * deltaX / distSq, -1.0),
            doubleArrayOf(0.0, 0.0, 1.0)
        ))
    }
}