package ca.joelathiessen.util

import ca.joelathiessen.kaly2.featuredetector.Feature
import java.util.*

fun getFeatureForPosition(robotX: Double, robotY: Double, robotTheta: Double, featureX: Double, featureY: Double,
                          angStdDev: Double = 0.0, distStdDev: Double = 0.0): Feature {
    val random = Random(2)

    // assume that the angle from the sensor is obtained accurately (e.g. with compass and or average particle pose)
    val angle = Math.atan2(featureY - robotY, featureX - robotX) + (random.nextGaussian() * angStdDev)
    val distance = Math.sqrt(Math.pow(featureX - robotX, 2.0) + Math.pow(featureY - robotY, 2.0)) + (random.nextGaussian() * distStdDev)

    return Feature(robotX, robotY, distance, angle, distStdDev)
}


val EPSILON = 0.000001
fun equals(a: Double, b: Double): Boolean {
    if (a == b) return true
    return Math.abs(a - b) < EPSILON * Math.max(Math.abs(a), Math.abs(b))
}