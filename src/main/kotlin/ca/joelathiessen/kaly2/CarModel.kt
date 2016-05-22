package ca.joelathiessen.kaly2

import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose

class CarModel : MotionModel {
    override fun moveRandom(particlePose: Pose, startRobotPose: RobotPose, endRobotPose: RobotPose) {

        val robotMoveAngle = startRobotPose.angleTo(endRobotPose.location)
        val deltaMoveAngle = robotMoveAngle - startRobotPose.heading
        val poseMoveAngle = (deltaMoveAngle + particlePose.heading)
        val robotMoveDist = endRobotPose.location.subtract(startRobotPose.location).length()

        val x = Math.cos(poseMoveAngle.toDouble()) * robotMoveDist
        val y = Math.sin(poseMoveAngle.toDouble()) * robotMoveDist

        val deltaHeading = endRobotPose.heading - startRobotPose.heading

        particlePose.location.add(Point(x.toFloat(), y.toFloat()))
        particlePose.heading += deltaHeading
    }

    fun applyControls(robotPose: RobotPose) {
    }
}