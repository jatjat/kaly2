package ca.joelathiessen.kaly2.subconscious

interface RobotPilot {
    val poses: PilotPoses
    val maxDist: Float
    val maxRot: Float

    fun execLocalPlan(plan: LocalPlan)
}