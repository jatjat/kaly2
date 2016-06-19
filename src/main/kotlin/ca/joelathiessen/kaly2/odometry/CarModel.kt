package ca.joelathiessen.kaly2.odometry

import ca.joelathiessen.kaly2.odometry.MotionModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose

class CarModel : MotionModel {
    override fun moveRandom(inputPose: Pose, startReadPose: RobotPose, endReadPose: RobotPose): Pose {

        val robotMoveAngle = startReadPose.angleTo(endReadPose.location)
        val deltaMoveAngle = robotMoveAngle - startReadPose.heading
        val poseMoveAngle = (deltaMoveAngle + inputPose.heading)
        val robotMoveDist = endReadPose.location.subtract(startReadPose.location).length()

        val dX = Math.cos(poseMoveAngle.toDouble()) * robotMoveDist
        val dY = Math.sin(poseMoveAngle.toDouble()) * robotMoveDist

        val dHeading = endReadPose.heading - startReadPose.heading

        val movedPose = Pose( inputPose.x + dX.toFloat(), inputPose.y + dY.toFloat(),
                inputPose.heading + dHeading)
        return movedPose
    }

    fun applyControls(robotPose: RobotPose) {
    }
}