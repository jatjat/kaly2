package ca.joelathiessen.kaly2.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.map.MapTree
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.linear.LinearPathSegmentRootFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GlobalPathPlannerTest {
    val EPSILON = 0.001

    @Test
    fun testNoObs() {
        val factory = LinearPathSegmentRootFactory()

        val obsTree = MapTree()
        val obsSize = 0.1f
        val searchDist = 1.2f
        val stepDist = 0.2f
        val start = RobotPose(0, 0f, 0f, 0f, 0f)
        val end = RobotPose(0, 0f, 1f, 1f, 0f)
        val itrs = 100

        val glbPthPln = GlobalPathPlanner(factory, obsTree, obsSize, searchDist, stepDist, start, end)
        glbPthPln.iterate(itrs)
        val bestPath = glbPthPln.getManeuvers()
        assertFalse(bestPath.isEmpty())
        assertEquals(bestPath.last().x, end.x, 0.2f)
        assertEquals(bestPath.last().y, end.y, 0.2f)
    }
}