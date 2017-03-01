package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.odometry.RobotPose

abstract class RobotPilot(private val startPose: RobotPose) {
    var odoPose = startPose
        protected set

    abstract fun execLocalPlan(plan: LocalPlan)
}