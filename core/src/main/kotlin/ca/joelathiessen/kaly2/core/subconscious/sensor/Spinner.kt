package ca.joelathiessen.kaly2.core.subconscious.sensor

import ca.joelathiessen.util.FloatMath
import lejos.robotics.RegulatedMotor

class Spinner {
    private var motor: RegulatedMotor
    private var maxDetectorAngleDeg: Int = 0
    private var minDetectorAngleDeg: Int = 0

    private var turnClockwise = true

    constructor(motor: RegulatedMotor, maxDetectorAngleDeg: Int, minDetectorAngleDeg: Int) {

        this.motor = motor
        this.maxDetectorAngleDeg = maxDetectorAngleDeg
        this.minDetectorAngleDeg = minDetectorAngleDeg
        motor.speed = DETECTOR_SPEED_DEGS_SEC
    }

    constructor(motor: RegulatedMotor) {

        this.motor = motor
        this.maxDetectorAngleDeg = DEFAULT_MAX_DETECTOR_ANGLE_DEG
        this.minDetectorAngleDeg = DEFAULT_MIN_DETECTOR_ANGLE_DEG
        motor.speed = DETECTOR_SPEED_DEGS_SEC
    }

    fun spin() {

        // spin the detector back and forth (if we spun one direction,
        // for the current sensor configuration the wires would jam):
        if (turnClockwise == true) {
            motor.rotateTo(maxDetectorAngleDeg, true)
            turnClockwise = false
        } else {
            motor.rotateTo(minDetectorAngleDeg, true)
            turnClockwise = true
        }
    }

    fun spinning(): Boolean {
        return motor.isMoving
    }

    val angle: Float
        get() {
            val tachoCount: Int = motor.tachoCount.toInt()
            return FloatMath.toRadians(tachoCount)
        }

    fun turningClockwise(): Boolean {
        return turnClockwise
    }

    companion object {
        val DETECTOR_SPEED_DEGS_SEC = 100
        val DEFAULT_MAX_DETECTOR_ANGLE_DEG = 180
        val DEFAULT_MIN_DETECTOR_ANGLE_DEG = -180
    }
}
