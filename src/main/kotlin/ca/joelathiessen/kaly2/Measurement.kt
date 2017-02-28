package ca.joelathiessen.kaly2

import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose

class Measurement(val distance: Float, val probAngle: Float, val probPose: Pose, val odoPose: Pose, val time: Long) {
    val dX = FloatMath.cos(probAngle) * distance
    val dY = FloatMath.sin(probAngle) * distance
    val x = probPose.x + dX
    val y = probPose.y + dY
}
