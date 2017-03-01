package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.FloatRandom
import ca.joelathiessen.util.rotate

class SimulatedPilot(private val odoAngStdDev: Float, private val odoDistStdDev: Float, private val stepDist: Float,
                     startPose: RobotPose, private val madeRealPose: (realPose: RobotPose) -> Unit) :
        RobotPilot(startPose) {

    private val random = FloatRandom(1)

    var realPose = startPose
        private set

    override fun execLocalPlan(plan: LocalPlan) {
        val realDist = Math.min(stepDist, plan.distance)

        val realRot = rotate(plan.angle, realDist, realPose.heading)
        realPose = RobotPose(0, 0f, realPose.x + realRot.deltaX, realPose.y + realRot.deltaY,
                realPose.heading + plan.angle)

        val odoAng = plan.angle + (odoAngStdDev * random.nextGaussian())
        val odoDistOffset = odoDistStdDev * random.nextGaussian()
        val odoRotRes = rotate(odoAng, realDist + odoDistOffset, odoPose.heading)
        odoPose = RobotPose(0, 0f, odoPose.x + odoRotRes.deltaX, odoPose.y + odoRotRes.deltaY,
                odoPose.heading + odoAng)

        madeRealPose(realPose)
    }
}