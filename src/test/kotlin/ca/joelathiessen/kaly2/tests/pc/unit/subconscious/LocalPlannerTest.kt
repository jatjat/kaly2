package ca.joelathiessen.kaly2.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.subconscious.LocalPlanner
import ca.joelathiessen.util.FloatMath
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class LocalPlannerTest {
    val EPSILON = 0.001f
    val DEFAULT_SENSOR_X = 0f
    val DEFAULT_SENSOR_Y = 0f

    fun makeMeasurement(x: Float, y: Float,
                        sensorPos: RobotPose = RobotPose(0, 0f, DEFAULT_SENSOR_X, DEFAULT_SENSOR_Y, 0f)): Measurement {
        val deltaX = x - sensorPos.x
        val deltaY = y - sensorPos.y
        val dist = FloatMath.sqrt((deltaX * deltaX) + (deltaY * deltaY))
        val ang = FloatMath.atan2(deltaY, deltaX)
        return Measurement(dist, ang, sensorPos, sensorPos, 0L)
    }

    @Test
    fun testZeroAng() {
        val staticObstacles = ArrayList<Measurement>()
        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0f
        val maxDist = 10.0f
        val distStep = 0.1f

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 3.5f
        val desAng = 0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25f, 0.1f, distStep, 5.0f, 100.0f, 1.0f)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 0.0f, EPSILON)
        assertEquals(plan.distance, 3.5f, EPSILON)
    }

    @Test
    fun testZeroDist() {
        val staticObstacles = ArrayList<Measurement>()
        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.1f
        val maxDist = 10.0f
        val distStep = 0.1f

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 0.0f
        val desAng = 1.0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25f, 0.1f, distStep, 5.0f, 100.0f, 1.0f)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 1.0f, EPSILON)
        assertEquals(plan.distance, 0.0f, EPSILON)
    }

    @Test
    fun testAngAndDist() {
        val staticObstacles = ArrayList<Measurement>()
        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.1f
        val maxDist = 10.0f
        val distStep = 0.1f

        val desiredPath = ArrayList<RobotPose>()
        val desAng = 1.0f
        desiredPath.add(RobotPose(1, 0f, 7f, 7f, desAng))

        val planner = LocalPlanner(0.25f, 0.1f, distStep, 5.0f, 100.0f, 1.0f)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 1.0f, EPSILON)
        assertEquals(plan.distance, 9.9f, EPSILON)
    }

    @Test
    fun testBlockedByVertLine() {
        val staticObstacles = ArrayList<Measurement>()
        for(y in -10 until 10 step 1) {
            staticObstacles += makeMeasurement(1.9f, y.toFloat())
        }

        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0f
        val maxDist = 10.0f
        val distStep = 0.1f

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 5.0f
        val desAng = 0.0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25f, 0.1f, distStep, 5.0f, 100.0f, 1.0f)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 0.0f, EPSILON)
        assertEquals(plan.distance, 0.6f, EPSILON)
    }

    @Test
    fun testGoAroundVertLine() {
        val staticObstacles = ArrayList<Measurement>()
        for(y in -10 until 1 step 1) {
            staticObstacles += makeMeasurement(4.9f, y - 0.6f)
        }

        val startPose = RobotPose(0, 0f, 0f, 0f, 0f)

        val maxAng = 1.0f
        val maxDist = 10.0f
        val distStep = 0.1f

        val desiredPath = ArrayList<RobotPose>()
        val desDist = 5.0f
        val desAng = 0.0f
        desiredPath.add(RobotPose(1, 0f, desDist, 0f, desAng))

        val planner = LocalPlanner(0.25f, 0.1f, distStep, 5.0f, 100.0f, 1.0f)
        val plan = planner.makePlan(staticObstacles, startPose, maxAng, maxDist, desiredPath)

        assertEquals(plan.angle, 0.4f, EPSILON)
        assertEquals(plan.distance, 4.9f, EPSILON)
    }
}