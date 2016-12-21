package ca.joelathiessen.kaly2.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.subconscious.LocalPlanner
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class LocalPlannerTest {
    val EPSILON = 0.001

    @Test
    fun testDistZeroAng() {
        val staticObstacles = ArrayList<Pair<Double, Double>>()
        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxDist = 10.0
        val distStep = 0.1

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 3.5f
        val desAng = 0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.5, 0.1, distStep, 5.0, 100.0, 1.0)
        val plan = planner.makePlan(staticObstacles, startPose, 3.0, maxDist, desiredPath)

        assertEquals(plan.angle, 0.0, EPSILON)
        assertEquals(plan.distance, 3.5, EPSILON)
    }
}