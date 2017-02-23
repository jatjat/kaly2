package ca.joelathiessen.kaly2.featuredetector

import Jama.Matrix
import ca.joelathiessen.util.FloatMath

open class Feature(val sensorX: Float, val sensorY: Float, val distance: Float, val angle: Float,
                   val stdDev: Float) {
    val dX = FloatMath.cos(angle) * distance
    val dY = FloatMath.sin(angle) * distance

    val x = dX + sensorX
    val y = dY + sensorY

    fun makeJacobian(): Matrix {
        val distanceAbs = FloatMath.abs(distance)
        val distSq = distanceAbs * distanceAbs
        val dX = x - sensorX
        val dY = y - sensorY
        return Matrix(arrayOf(
                doubleArrayOf(-1.0 * dX / distanceAbs, -1.0 * dY / distanceAbs, 0.0),
                doubleArrayOf((dY / distSq).toDouble(), -1.0 * dX / distSq, -1.0),
                doubleArrayOf(0.0, 0.0, 1.0)
        ))
    }
}