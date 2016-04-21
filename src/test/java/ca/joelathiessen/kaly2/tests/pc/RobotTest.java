package ca.joelathiessen.kaly2.tests.pc;

import ca.joelathiessen.kaly2.Robot;
import ca.joelathiessen.kaly2.subconscious.Measurement;
import ca.joelathiessen.kaly2.subconscious.Subconscious;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;

public class RobotTest {

    Robot robot;

    @Before
    public void setUp() {
        Subconscious sub = Mockito.mock(Subconscious.class);
        ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps =
                new ConcurrentLinkedQueue<ArrayList<Measurement>>();
        robot = new Robot(sub, sweeps);
    }

    @Test
    public void testRobot() {
        assertNotNull(robot); // is this sort of test actually necessary?
    }

    @Test
    public void testConstructRobot_PassedValuesUnmodified() {

        Subconscious sub = Mockito.mock(Subconscious.class);
        ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps =
                new ConcurrentLinkedQueue<ArrayList<Measurement>>();

        Subconscious subCopy = sub;
        ConcurrentLinkedQueue<ArrayList<Measurement>> sweepsCopy = sweeps;

        Robot robot2 = new Robot(sub, sweeps);
        robot2.toString();

        assertEquals(sub, subCopy);
        assertEquals(sweeps, sweepsCopy);
    }

    @Test
    public void testStartStopRobotSimple() {
        robot.startRobot();

        assertTrue(robot.isRunning());

        robot.stopRobot();

        assertFalse(robot.isRunning());
    }

    @Test
    public void testStartStopRobotRunning() {
        Thread thread = new Thread(robot);
        thread.start();
        robot.startRobot();

        assertTrue(robot.isRunning());

        robot.stopRobot();

        assertFalse(robot.isRunning());

        thread.interrupt();

        try {
            thread.join(150);
        } catch (InterruptedException e) {
            fail("Robot thread was interupted but didn't stop in a timely fashion");
        }
    }

}
