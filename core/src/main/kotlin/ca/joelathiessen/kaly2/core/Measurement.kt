package ca.joelathiessen.kaly2.core

import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose

class Measurement(val distance: Float, val probAngle: Float, val probPose: Pose, val odoPose: Pose, val time: Long) {
    val deltaX = FloatMath.cos(probAngle) * distance
    val deltaY = FloatMath.sin(probAngle) * distance
    val x = probPose.x + deltaX
    val y = probPose.y + deltaY
}
