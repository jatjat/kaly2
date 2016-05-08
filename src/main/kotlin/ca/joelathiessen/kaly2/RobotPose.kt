package ca.joelathiessen.kaly2

import lejos.robotics.navigation.Pose

class RobotPose(val time: Int, val rotRate: Float, x: Float, y: Float, heading: Float) : Pose(x, y, heading) {
}