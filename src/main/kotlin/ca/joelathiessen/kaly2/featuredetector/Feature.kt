package ca.joelathiessen.kaly2.featuredetector

import Jama.Matrix

open class Feature(val sensorX: Double, val sensorY: Double, val distance: Double, val angle: Double,
                   val stdDev: Double) {
    val dX = Math.cos(angle) * distance
    val dY = Math.sin(angle) * distance

    val x = dX + sensorX
    val y = dY + sensorY

    private lateinit var oneCalcJacob: Matrix
    private var madeOneCalcJacob = false

    val jacobian: Matrix
        get() {
            if (!madeOneCalcJacob) {
                val distSq = distance * distance
                val dX = x - sensorX
                val dY = y - sensorY
                oneCalcJacob = Matrix(arrayOf(
                        doubleArrayOf(-1.0 * dX / distance, -1.0 * dY / distance, 0.0),
                        doubleArrayOf(dY / distSq, -1.0 * dX / distSq, -1.0),
                        doubleArrayOf(0.0, 0.0, 1.0)
                ))
                madeOneCalcJacob = true
            }
            return oneCalcJacob
        }
}