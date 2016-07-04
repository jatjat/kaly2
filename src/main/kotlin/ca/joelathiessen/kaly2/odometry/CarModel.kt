package ca.joelathiessen.kaly2.odometry

import Jama.Matrix
import lejos.robotics.navigation.Pose
import java.util.*

class CarModel : MotionModel {
    private val ANGLE_ERROR = 0.0005
    private val DIST_ERROR = 0.5
    private val random = Random()

    override fun moveRandom(inputPose: Pose, startReadPose: RobotPose, endReadPose: RobotPose): Pose {

        val robotMoveAngle = Math.toRadians(startReadPose.angleTo(endReadPose.location).toDouble())
        val dMoveAngStartPoseHead = robotMoveAngle - startReadPose.heading

        val angleAdjust = random.nextGaussian() * ANGLE_ERROR

        val moveAngle = (inputPose.heading + dMoveAngStartPoseHead) //+ angleAdjust
        val moveDist = startReadPose.location.distance(endReadPose.location) + random.nextGaussian() * DIST_ERROR

        val dX = Math.cos(moveAngle.toDouble()) * moveDist
        val dY = Math.sin(moveAngle.toDouble()) * moveDist
        val dHeading = endReadPose.heading - startReadPose.heading //+ angleAdjust

        val movedPose = Pose((inputPose.x + dX).toFloat(), (inputPose.y + dY).toFloat(), (inputPose.heading + dHeading).toFloat())
        return movedPose
    }

    fun applyControls(robotPose: RobotPose) {
    }
}