package ca.joelathiessen.kaly2.subconscious

interface RobotPilot {
    val poses: PilotPoses

    fun execLocalPlan(plan: LocalPlan)
}