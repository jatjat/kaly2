package ca.joelathiessen.kaly2.core.ev3

sealed class RobotCommand(val type: String)
data class RobotPilotCommand(val steerAngle: Float, val travelSpeed: Float) : RobotCommand("RobotPilot")
data class RobotSpinnerCommand(val spin: Boolean) : RobotCommand("RobotSpinner")
