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
    fun testZeroAng() {
        val staticObstacles = ArrayList<Pair<Double, Double>>()
        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0
        val maxDist = 10.0
        val distStep = 0.1

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 3.5f
        val desAng = 0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25, 0.1, distStep, 5.0, 100.0, 1.0)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 0.0, EPSILON)
        assertEquals(plan.distance, 3.5, EPSILON)
    }

    @Test
    fun testZeroDist() {
        val staticObstacles = ArrayList<Pair<Double, Double>>()
        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0
        val maxDist = 10.0
        val distStep = 0.1

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 0.0f
        val desAng = 1.0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25, 0.1, distStep, 5.0, 100.0, 1.0)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 1.0, EPSILON)
        assertEquals(plan.distance, 0.0, EPSILON)
    }

    @Test
    fun testAngAndDist() {
        val staticObstacles = ArrayList<Pair<Double, Double>>()
        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0
        val maxDist = 10.0
        val distStep = 0.1

        val desiredPath = ArrayList<RobotPose>()
        val desAng = 1.0f
        desiredPath.add(RobotPose(1, 0f, 7f, 7f, desAng))

        val planner = LocalPlanner(0.25, 0.1, distStep, 5.0, 100.0, 1.0)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 1.0, EPSILON)
        assertEquals(plan.distance, 9.9, EPSILON)
    }

    @Test
    fun testBlockedByVertLine() {
        val staticObstacles = ArrayList<Pair<Double, Double>>()
        for(y in -10 until 10 step 1) {
            staticObstacles.add(Pair(1.9, y.toDouble()))
        }

        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0
        val maxDist = 10.0
        val distStep = 0.1

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 5.0f
        val desAng = 0.0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25, 0.1, distStep, 5.0, 100.0, 1.0)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 0.0, EPSILON)
        assertEquals(plan.distance, 0.6, EPSILON)
    }

    @Test
    fun testGoAroundVertLine() {
        val staticObstacles = ArrayList<Pair<Double, Double>>()
        for(y in -10 until 1 step 1) {
            staticObstacles.add(Pair(4.9, y.toDouble() - 0.6))
        }

        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0
        val maxDist = 10.0
        val distStep = 0.1

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 5.0f
        val desAng = 0.0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25, 0.1, distStep, 5.0, 100.0, 1.0)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 0.4, EPSILON)
        assertEquals(plan.distance, 4.9, EPSILON)
    }
}