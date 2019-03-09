package ca.joelathiessen.kaly2.core.subconscious

interface RobotPilot {
    val poses: PilotPoses
    val maxDist: Float
    val maxRot: Float

    fun execLocalPlan(plan: LocalPlan)
}