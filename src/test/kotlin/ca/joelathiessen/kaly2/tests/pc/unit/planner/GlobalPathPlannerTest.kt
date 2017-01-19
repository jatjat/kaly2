package ca.joelathiessen.kaly2.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.linear.LinearPathSegmentRootFactory
import ca.joelathiessen.util.GenTree
import lejos.robotics.geometry.Point
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
        val glbPthPln = GlobalPathPlanner(factory)

        val obsTree = GenTree<Point>()
        val obsSize = 0.1
        val searchDist = 5.0
        val stepDist = 0.1
        val start = RobotPose(0, 0f, 0f, 0f, 0f)
        val end = RobotPose(0, 0f, 1f, 1f, 0f)
        val itrs = 1

        val bestPath = glbPthPln.getPath(obsTree, obsSize, searchDist, stepDist, start, end, itrs)

        assertFalse(bestPath.isEmpty())
    }
}