package ca.joelathiessen.kaly2.tests.pc.unit.slam

import ca.joelathiessen.kaly2.featuredetector.Feature
import java.util.*

fun getFeatureForPosition(robotX: Double, robotY: Double, RobotTheta: Double, featureX: Double, featureY: Double,
                          stdDev: Double = 0.0): Feature {
    val random = Random(2)

    val angle = Math.atan2(featureY-robotY, featureX-robotX) //+ random.nextGaussian() * stdDev
    val distance = Math.sqrt(Math.pow(featureX-robotX, 2.0) + Math.pow(featureY-robotY, 2.0)) //+ random.nextGaussian() * stdDev
    return Feature(robotX, robotY, distance, angle, stdDev)
}