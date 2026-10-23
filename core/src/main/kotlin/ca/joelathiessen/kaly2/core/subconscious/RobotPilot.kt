package ca.joelathiessen.kaly2.core.subconscious

interface RobotPilot {
    val poses: PilotPoses
    val maxDesiredPlanDist: Float
    val maxDesiredPlanRot: Float

    fun execLocalPlan(plan: LocalPlan)
}