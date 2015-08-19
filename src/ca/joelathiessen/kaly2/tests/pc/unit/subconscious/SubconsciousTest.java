package ca.joelathiessen.kaly2.tests.pc.unit.subconscious;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;

import org.junit.Test;
import org.mockito.Mockito;

import ca.joelathiessen.kaly2.main.Robot;
import ca.joelathiessen.kaly2.main.subconscious.Measurement;
import ca.joelathiessen.kaly2.main.subconscious.Subconscious;
import ca.joelathiessen.kaly2.main.subconscious.sensor.JoelPulsedLightLidarLiteV2;
import ca.joelathiessen.kaly2.main.subconscious.sensor.Spinner;
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

  @Test
  public void testRun_1SweepOf3Measurements() {

    long timeout = System.currentTimeMillis() + 1000;
    ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps =
        new ConcurrentLinkedQueue<ArrayList<Measurement>>();
    JoelPulsedLightLidarLiteV2 sensor = Mockito.mock(JoelPulsedLightLidarLiteV2.class);
    DifferentialPilot pilot = Mockito.mock(DifferentialPilot.class);
    OdometryPoseProvider odometry = Mockito.mock(OdometryPoseProvider.class);
    Spinner spinner = Mockito.mock(Spinner.class);
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
}
