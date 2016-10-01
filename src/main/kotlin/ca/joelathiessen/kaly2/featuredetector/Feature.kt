package ca.joelathiessen.kaly2.featuredetector

import Jama.Matrix
import lejos.robotics.navigation.Pose

open class Feature(val sensorX: Double, val sensorY: Double, val distance: Double, val angle: Double,
                   val stdDev: Double) {
    val dX = Math.cos(angle) * distance
    val dY = Math.sin(angle) * distance

    val x = dX + sensorX
    val y = dY + sensorY

    fun makeJacobian(): Matrix {
        val distanceAbs = Math.abs(distance)
        val distSq = distanceAbs * distanceAbs
        val dX = x - sensorX
        val dY = y - sensorY
        return Matrix(arrayOf(
                doubleArrayOf(-1.0 * dX / distanceAbs, -1.0 * dY / distanceAbs, 0.0),
                doubleArrayOf(dY / distSq, -1.0 * dX / distSq, -1.0),
                doubleArrayOf(0.0, 0.0, 1.0)
        ))
    }
}