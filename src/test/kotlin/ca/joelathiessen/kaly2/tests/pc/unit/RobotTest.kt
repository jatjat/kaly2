package ca.joelathiessen.kaly2.tests.pc.unit

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.Robot
import ca.joelathiessen.kaly2.subconscious.Subconscious
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.util.ArrayList
import java.util.concurrent.ConcurrentLinkedQueue

@RunWith(PowerMockRunner::class)
@PrepareForTest(Subconscious::class, Measurement::class)
class RobotTest {

    internal lateinit var robot: Robot

    @Before
    fun setUp() {
        val sub = Mockito.mock(Subconscious::class.java)
        val sweeps = ConcurrentLinkedQueue<ArrayList<Measurement>>()
        robot = Robot(sub, sweeps)
    }

    @Test
    fun testRobot() {
        assertNotNull(robot) // is this sort of test actually necessary?
    }

    @Test
    fun testConstructRobot_PassedValuesUnmodified() {
        val sub = Mockito.mock(Subconscious::class.java)
        val sweeps = ConcurrentLinkedQueue<ArrayList<Measurement>>()

        val subCopy = sub
        val sweepsCopy = sweeps

        val robot2 = Robot(sub, sweeps)
        robot2.toString()

        assertEquals(sub, subCopy)
        assertEquals(sweeps, sweepsCopy)
    }

    @Test
    fun testStartStopRobotSimple() {
        robot.startRobot()

        assertTrue(robot.isRunning)

        robot.stopRobot()

        assertFalse(robot.isRunning)
    }

    @Test
    fun testStartStopRobotRunning() {
        val thread = Thread(robot)
        thread.start()
        robot.startRobot()

        assertTrue(robot.isRunning)

        robot.stopRobot()

        assertFalse(robot.isRunning)

        thread.interrupt()

        try {
            thread.join(150)
        } catch (e: InterruptedException) {
            fail("Robot thread was interupted but didn't stop in a timely fashion")
        }

    }

}
