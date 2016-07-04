package ca.joelathiessen.kaly2.featuredetector

import Jama.Matrix
import lejos.robotics.navigation.Pose

open class Feature(val sensorX: Double, val sensorY: Double, val distance: Double, val angle: Double,
                   val stdDev: Double) {
    val dX = Math.cos(angle) * distance
    val dY = Math.sin(angle) * distance

    val x = dX + sensorX
    val y = dY + sensorY

    fun  makeJacobian(pose: Pose): Matrix {
        val distSq = distance * distance
        val dX = x - pose.x
        val dY = y - pose.y
        return Matrix(arrayOf(
                doubleArrayOf(-1.0 * dX / distance, -1.0 * dY / distance, 0.0),
                doubleArrayOf(dY / distSq, -1.0 * dX / distSq, -1.0),
                doubleArrayOf(0.0, 0.0, 1.0)
        ))
    }
}