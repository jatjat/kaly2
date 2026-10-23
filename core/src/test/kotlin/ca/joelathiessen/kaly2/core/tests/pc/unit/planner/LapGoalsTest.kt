package ca.joelathiessen.kaly2.core.tests.pc.unit.planner

import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.planner.LapGoals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LapGoalsTest {
    private val corners = listOf(pose(0f, 0f), pose(100f, 0f), pose(100f, 100f))

    private fun pose(x: Float, y: Float) = RobotPose(0, 0f, x, y, 0f)

    @Test
    fun startsAtTheFirstPoint() {
        assertEquals(0f, LapGoals(corners, 10f).goal.x)
    }

    @Test
    fun keepsTheGoalUntilItIsReached() {
        val laps = LapGoals(corners, 10f)
        assertNull(laps.next(pose(50f, 50f)))
        assertEquals(0f, laps.goal.x)
    }

    @Test
    fun movesToTheNextPointOnceCloseEnough() {
        val laps = LapGoals(corners, 10f)
        val next = laps.next(pose(5f, 5f))
        assertEquals(100f, next!!.x)
        assertEquals(0f, next.y)
    }

    @Test
    fun goesBackToTheStartAfterTheLastPoint() {
        val laps = LapGoals(corners, 10f)
        laps.next(pose(0f, 0f))
        laps.next(pose(100f, 0f))
        val next = laps.next(pose(100f, 100f))
        assertEquals(0f, next!!.x)
        assertEquals(0f, next.y)
    }
}
