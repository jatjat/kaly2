package ca.joelathiessen.kaly2

import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose

class Measurement(val distance: Float, val angle: Float, val pose: Pose, val time: Long) {
    val dX = FloatMath.cos(angle) * distance
    val dY = FloatMath.sin(angle) * distance
    val x = pose.x + dX
    val y = pose.y + dY
}
