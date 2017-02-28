package ca.joelathiessen.kaly2.odometry

import lejos.robotics.navigation.Pose
import ca.joelathiessen.util.*

class CarModel : MotionModel {
    private val ANGLE_ERROR = 0.01f
    private val DIST_ERROR = 0.5f
    private val random = FloatRandom(0)

    override fun moveRandom(inputPose: Pose, startReadPose: RobotPose, endReadPose: RobotPose): Pose {

        val robotMoveAngle = FloatMath.toRadians(startReadPose.angleTo(endReadPose.location))
        val dMoveAngStartPoseHead = robotMoveAngle - startReadPose.heading

        val angleAdjust = random.nextGaussian() * ANGLE_ERROR

        val moveAngle = (inputPose.heading + dMoveAngStartPoseHead) + angleAdjust
        val moveDist = startReadPose.distanceTo(endReadPose.location) + (random.nextGaussian() * DIST_ERROR)

        val deltaX = FloatMath.cos(moveAngle) * moveDist
        val deltaY = FloatMath.sin(moveAngle) * moveDist
        val dHeading = endReadPose.heading - startReadPose.heading + angleAdjust

        val movedPose = Pose((inputPose.x + deltaX), (inputPose.y + deltaY), (inputPose.heading + dHeading))
        return movedPose
    }
}