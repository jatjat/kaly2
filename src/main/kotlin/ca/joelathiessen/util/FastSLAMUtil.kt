package ca.joelathiessen.util

import ca.joelathiessen.kaly2.featuredetector.Feature
import java.util.*

fun getFeatureForPosition(robotX: Float, robotY: Float, @Suppress("UNUSED_PARAMETER") robotTheta: Float,
                          featureX: Float, featureY: Float, angStdDev: Float = 0.0f, distStdDev: Float = 0.0f): Feature {
    val random = FloatRandom(2)

    // assume that the angle from the sensor is obtained accurately (e.g. with compass and or average particle pose)
    val dX = featureX - robotX
    val dY = featureY - robotY
    val angle = FloatMath.atan2(dY, dX) + (random.nextGaussian() * angStdDev)
    val distance = FloatMath.sqrt((dX * dX) + (dY * dY)) + (random.nextGaussian() * distStdDev)
    return Feature(robotX, robotY, distance, angle, dX, dY, distStdDev)
}