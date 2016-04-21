package ca.joelathiessen.kaly2.tests.pc.unit.subconscious;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.I2CSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;

import org.junit.Test;
import org.mockito.Mockito;

import ca.joelathiessen.kaly2.Robot;
import ca.joelathiessen.kaly2.subconscious.Measurement;
import ca.joelathiessen.kaly2.subconscious.Subconscious;
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2PulsedLightLidarLiteV2;
import ca.joelathiessen.kaly2.subconscious.sensor.Spinner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class SubconsciousTest {

  private Kaly2PulsedLightLidarLiteV2 sensor;
  private Spinner spinner;
  private DifferentialPilot pilot;
  private OdometryPoseProvider odometry;
  private ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps;

  @Before
  public void setUp() {
    sensor = Mockito.mock(Kaly2PulsedLightLidarLiteV2.class);
    pilot = Mockito.mock(DifferentialPilot.class);
    odometry = Mockito.mock(OdometryPoseProvider.class);
    RegulatedMotor motor = Mockito.mock(RegulatedMotor.class);
    spinner = Mockito.mock(Spinner.class);
    sweeps = new ConcurrentLinkedQueue<ArrayList<Measurement>>();
  }

  @Test
  public void testConstructSubconscious_PassedValuesUnmodified() {

    String sensorCopy = sensor.toString();
    String spinCopy = spinner.toString();
    String pilotCopy = pilot.toString();
    String odoCopy = odometry.toString();
    String sweepsCopy = sweeps.toString();

    Subconscious sub = new Subconscious(sensor, pilot, odometry, spinner, sweeps);
    sub.toString();

    assertEquals(sensor.toString(), sensorCopy);
    assertEquals(pilot.toString(), pilotCopy);
    assertEquals(odometry.toString(), odoCopy);
    assertEquals(spinner.toString(), spinCopy);
    assertEquals(sweeps.toString(), sweepsCopy);
  }

  @Test
  public void testRun_atLeast1SweepOf3Measurements() {

    long timeout;
    Subconscious sub = new Subconscious(sensor, pilot, odometry, spinner, sweeps);
    Thread thread = new Thread(sub);
    
    when(odometry.getPose()).thenReturn(new Pose());
    // spin thrice:
    when(spinner.spinning()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
    when(spinner.getAngle()).thenReturn(2f);
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) throws Throwable {
        float[] sample = (float[]) invocation.getArguments()[0];
        sample[0] = 10;
        return null;
      }
    }).when(sensor).fetchSample(any(float[].class), anyInt());

    timeout = System.currentTimeMillis() + 1000;
    thread.start();
    // I think polling is fine, at least for now:
    while (sweeps.isEmpty() == true && System.currentTimeMillis() < timeout) {
    }
    thread.interrupt();

    assertFalse("At least one sensor sweep should have been created", sweeps.isEmpty());

    for (ArrayList<Measurement> sweep : sweeps) {
      for (Measurement mes : sweep) {
        assertEquals("Measurement's distance should be correct", mes.getDistance(), 10, 0);
        assertEquals("Measurement's angle should be correct", mes.getAngle(), 2, 0);
        assertNotNull("Measurement's pose should not be null", mes.getPose());

        assertTrue("Measurement's time should be correct", mes.getTime() <= timeout);
        // well it works on my computer!
      }

    }
  }
  
  @Test
  public void testRun_interruptionHaltsExcecution() {
    Subconscious sub = new Subconscious(sensor, pilot, odometry, spinner, sweeps);
    Thread thread = new Thread(sub);
    thread.start();
    thread.interrupt();
    try {
      thread.join(150);
    } catch (InterruptedException e) {
      fail("Subconscious thread was interupted but didn't stop in a timely fashion");
    }
  }
}