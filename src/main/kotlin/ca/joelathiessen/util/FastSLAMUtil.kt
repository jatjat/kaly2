package ca.joelathiessen.util

import ca.joelathiessen.kaly2.featuredetector.Feature
import java.util.*

fun getFeatureForPosition(robotX: Float, robotY: Float, robotTheta: Float, featureX: Float, featureY: Float,
                          angStdDev: Float = 0.0f, distStdDev: Float = 0.0f): Feature {
    val random = FloatRandom(2)

    // assume that the angle from the sensor is obtained accurately (e.g. with compass and or average particle pose)
    val angle = FloatMath.atan2(featureY - robotY, featureX - robotX) + (random.nextGaussian() * angStdDev)
    val distance = FloatMath.sqrt(FloatMath.pow(featureX - robotX, 2.0f) + FloatMath.pow(featureY - robotY, 2.0f)) + (random.nextGaussian() * distStdDev)

    return Feature(robotX, robotY, distance, angle, distStdDev)
}