package ca.joelathiessen.kaly2.core.ev3

data class RobotInfo(
    val steerAngle: Float,
    val sensorAngle: Float,
    val sensorSpinning: Boolean,
    val travelAngle: Float,
    val travelSpeed: Float
)