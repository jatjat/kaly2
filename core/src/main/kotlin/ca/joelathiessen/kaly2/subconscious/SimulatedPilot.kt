package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.FloatRandom
import ca.joelathiessen.util.rotate

class SimulatedPilot(
    private val odoAngStdDev: Float,
    private val odoDistStdDev: Float,
    private val stepDist: Float,
    startPose: RobotPose,
    override val maxRot: Float,
    override val maxDist: Float
) : RobotPilot {

    private val random = FloatRandom(1)

    override var poses: SimPilotPoses = SimPilotPoses(startPose, startPose)
        private set

    override fun execLocalPlan(plan: LocalPlan) {
        val realDist = Math.min(stepDist, plan.distance)

        val realRot = rotate(plan.angle, realDist, poses.realPose.heading)
        val realPose = RobotPose(0, 0f, poses.realPose.x + realRot.deltaX, poses.realPose.y + realRot.deltaY,
            poses.realPose.heading + plan.angle)

        val odoAng = plan.angle + (odoAngStdDev * random.nextGaussian())
        val odoDistOffset = odoDistStdDev * random.nextGaussian()
        val odoRotRes = rotate(odoAng, realDist + odoDistOffset, poses.odoPose.heading)
        val odoPose = RobotPose(0, 0f, poses.odoPose.x + odoRotRes.deltaX, poses.odoPose.y + odoRotRes.deltaY,
            poses.odoPose.heading + odoAng)
        poses = SimPilotPoses(realPose, odoPose)
    }
}