package ca.joelathiessen.kaly2.core.tests.pc.unit.util

import ca.joelathiessen.kaly2.core.Measurement
import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose

fun makeMeasFromXY(x: Float, y: Float, robotX: Float, robotY: Float): Measurement {
    val xOffset = x - robotX
    val yOffset = y - robotY
    val dist = FloatMath.sqrt((xOffset * xOffset) + (yOffset * yOffset))
    val angle = FloatMath.atan2(yOffset, xOffset)
    val pose = Pose(robotX, robotY, 0f)
    return Measurement(dist, angle, pose, pose, 0)
}
