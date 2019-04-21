package ca.joelathiessen.kaly2.ev3

val MSG_TYPE = "type"
val ROBOT_PILOT = "RobotPilot"
val ROBOT_SPINNER = "RobotSpinner"

sealed class RobotCommand(val type: String)
data class RobotPilotCommand(val steerAngle: Float, val travelSpeed: Float) : RobotCommand(ROBOT_PILOT)
data class RobotSpinnerCommand(val spin: Boolean) : RobotCommand(ROBOT_SPINNER)
