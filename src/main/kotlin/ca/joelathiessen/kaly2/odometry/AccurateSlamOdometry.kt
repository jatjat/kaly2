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
        val dX = newOdoPose.x - odoPose.x
        val dY = newOdoPose.y - odoPose.y
        val dHeading = newOdoPose.heading - odoPose.heading

        val rotDif = slamPose.heading - odoPose.heading
        val rotX = (FloatMath.cos(rotDif) - FloatMath.sin(rotDif)) * dX
        val rotY = (FloatMath.sin(rotDif) + FloatMath.cos(rotDif)) * dY

        return RobotPose(0, 0f, slamPose.x + rotX, slamPose.y + rotY, slamPose.heading + dHeading)
    }
}