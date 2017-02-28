package ca.joelathiessen.kaly2.odometry

import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose

class AccurateSlamOdometry(initialPose: Pose, private val getOdometry: () -> Pose) {
    private var odoPose = initialPose
    private var slamPose = initialPose

    @Synchronized
    fun setInputPoses(slamPoseInput: Pose, odometricPoseInput: Pose) {
        slamPose = slamPoseInput
        odoPose = odometricPoseInput
    }

    @Synchronized
    fun getOutputPose(): RobotPose {
        val newOdoPose = getOdometry()
        val deltaX = newOdoPose.x - odoPose.x
        val deltaY = newOdoPose.y - odoPose.y
        val dHeading = newOdoPose.heading - odoPose.heading

        val rotDif = slamPose.heading - odoPose.heading
        val rotX = (FloatMath.cos(rotDif) - FloatMath.sin(rotDif)) * deltaX
        val rotY = (FloatMath.sin(rotDif) + FloatMath.cos(rotDif)) * deltaY

        return RobotPose(0, 0f, slamPose.x + rotX, slamPose.y + rotY, slamPose.heading + dHeading)
    }
}