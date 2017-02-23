package ca.joelathiessen.kaly2

import lejos.robotics.navigation.Pose

class Measurement(val distance: Float, val angle: Float, val pose: Pose, val time: Long)
