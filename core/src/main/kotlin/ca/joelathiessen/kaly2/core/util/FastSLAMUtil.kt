package ca.joelathiessen.util

import ca.joelathiessen.kaly2.core.featuredetector.Feature

fun getFeatureForPosition(
    robotX: Float,
    robotY: Float,
    @Suppress("UNUSED_PARAMETER") robotTheta: Float,
    featureX: Float,
    featureY: Float,
    angStdDev: Float = 0.0f,
    distStdDev: Float = 0.0f
): Feature {
    val random = FloatRandom(2)

    // assume that the angle from the sensor is obtained accurately (e.g. with compass and or average particle pose)
    val deltaX = featureX - robotX
    val deltaY = featureY - robotY
    val angle = FloatMath.atan2(deltaY, deltaX) + (random.nextGaussian() * angStdDev)
    val distance = FloatMath.sqrt((deltaX * deltaX) + (deltaY * deltaY)) + (random.nextGaussian() * distStdDev)
    return Feature(robotX, robotY, distance, angle, deltaX, deltaY, distStdDev)
}