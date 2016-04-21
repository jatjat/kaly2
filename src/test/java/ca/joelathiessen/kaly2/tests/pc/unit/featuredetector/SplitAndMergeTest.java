package ca.joelathiessen.kaly2.tests.pc.unit.featuredetector;

import ca.joelathiessen.kaly2.featuredetector.Kaly2Feature;
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge;
import ca.joelathiessen.kaly2.featuredetector.SplitAndMergeFeature;
import ca.joelathiessen.kaly2.subconscious.Measurement;

import java.util.Iterator;
import java.util.List;
import ca.joelathiessen.kaly2.subconscious.Subconscious;
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2PulsedLightLidarLiteV2;
import ca.joelathiessen.kaly2.subconscious.sensor.Spinner;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SplitAndMergeTest {

  private Measurement makeMeasFromXY(float x, float y) {
    return new Measurement((float)Math.sqrt(x*x + y*y),(float)Math.atan2(y,x), null, 0);
  }

  @Test
  public void testFourPointSquare() {
    SplitAndMerge merge = new SplitAndMerge();

    ArrayList<Measurement> measurements = new ArrayList<>();
    measurements.add(makeMeasFromXY(0,0));
    measurements.add(makeMeasFromXY(0,1));
    measurements.add(makeMeasFromXY(1,1));
    measurements.add(makeMeasFromXY(1,0));

    List<? extends Kaly2Feature> features = merge.getFeatures(measurements);

    assertTrue(features.size() == measurements.size());

    assertEquals(features.get(0).getX(), 0, 0.0001);
    assertEquals(features.get(0).getY(), 0, 0.0001);

    assertEquals(features.get(1).getX(), 0, 0.0001);
    assertEquals(features.get(1).getY(), 1, 0.0001);

    assertEquals(features.get(2).getX(), 1, 0.0001);
    assertEquals(features.get(2).getY(), 1, 0.0001);

    assertEquals(features.get(3).getX(), 1, 0.0001);
    assertEquals(features.get(3).getY(), 0, 0.0001);
  }

  @Test
  public void testFourPointLine() {
    SplitAndMerge merge = new SplitAndMerge();

    ArrayList<Measurement> measurements = new ArrayList<>();
    measurements.add(makeMeasFromXY(0,0));
    measurements.add(makeMeasFromXY(0,1));
    measurements.add(makeMeasFromXY(0,2));
    measurements.add(makeMeasFromXY(0,3));

    List<? extends Kaly2Feature> features = merge.getFeatures(measurements);

    assertTrue(features.size() == 2);
  }

  @Test
  public void testInnerCorner() {
    SplitAndMerge merge = new SplitAndMerge();

    ArrayList<Measurement> measurements = new ArrayList<>();
    for(float i = 0; i<= 1; i+= 0.1) {
      measurements.add(makeMeasFromXY(1,i));
    }
    for(float i = 0; i<= 1; i+= 0.1) {
      measurements.add(makeMeasFromXY(i,1));
    }

    List<? extends Kaly2Feature> features = merge.getFeatures(measurements);

    assertTrue(features.size() == 3);
  }

  @Test
  public void testOuterCorner() {
    SplitAndMerge merge = new SplitAndMerge();

    ArrayList<Measurement> measurements = new ArrayList<>();
    for(float i = 1; i>= 0; i-= 0.1) {
      measurements.add(makeMeasFromXY(0,i));
    }
    for(float i = 1; i>= 0; i-= 0.1) {
      measurements.add(makeMeasFromXY(i,0));
    }

    List<? extends Kaly2Feature> features = merge.getFeatures(measurements);

    assertTrue(features.size() == 3);
  }

  /*
  This test will work once the full split-and-merge is implemented
  @Test
  public void testDenseSquare() {
    SplitAndMerge merge = new SplitAndMerge();

    ArrayList<Measurement> measurements = new ArrayList<>();
    for(float i = -1; i<= 1; i+= 0.1) {
      measurements.add(makeMeasFromXY(-1,i));
    }
    for(float i = -1; i<= 1; i+= 0.1) {
      measurements.add(makeMeasFromXY(i,1));
    }
    for(float i = 1; i >= -1; i-= 0.1) {
      measurements.add(makeMeasFromXY(1,i));
    }
    for(float i = 1; i >= -1; i-= 0.1) {
      measurements.add(makeMeasFromXY(i,-1));
    }

    List<? extends Kaly2Feature> features = merge.getFeatures(measurements);

    assertTrue(features.size() == 4);
  }
  */
}
