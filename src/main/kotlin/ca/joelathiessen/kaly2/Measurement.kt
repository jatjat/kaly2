package ca.joelathiessen.kaly2

import lejos.robotics.navigation.Pose

class Measurement(val distance: Double, val angle: Double, val pose: Pose, val time: Long)
