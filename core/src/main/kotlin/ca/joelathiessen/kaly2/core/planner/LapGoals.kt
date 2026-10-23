package ca.joelathiessen.kaly2.core.planner

import ca.joelathiessen.kaly2.core.odometry.RobotPose

// Goals that take the robot around the same loop again and again, moving on to the next
// point once the robot is within reachedDist of the current one
class LapGoals(private val points: List<RobotPose>, private val reachedDist: Float) {
    private var current = 0

    val goal: RobotPose
        get() = points[current]

    // The next goal if the robot has reached the current one, otherwise null
    fun next(pose: RobotPose): RobotPose? {
        if (Math.hypot((pose.x - goal.x).toDouble(), (pose.y - goal.y).toDouble()) > reachedDist) {
            return null
        }
        current = (current + 1) % points.size
        return goal
    }
}
