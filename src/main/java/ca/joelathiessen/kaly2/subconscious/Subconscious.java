package ca.joelathiessen.kaly2.subconscious;

import java.util.ArrayList;
import java.util.List;

import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2PulsedLightLidarLiteV2;
import ca.joelathiessen.kaly2.subconscious.sensor.Spinner;
import lejos.hardware.motor.Motor;
import lejos.hardware.sensor.I2CSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Subconscious implements Runnable {

  // probable maximum number of measurements we will get per
  // 350 degree spin of the distance detector
  public static final int PROBABLE_MAX_MEASUREMENTS_PER_SWEEP = 360;

  private Kaly2PulsedLightLidarLiteV2 sensor;
  private Spinner spinner;
  private DifferentialPilot pilot;
  private OdometryPoseProvider odometry;
  private ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps;

  public Subconscious(Kaly2PulsedLightLidarLiteV2 sensor, DifferentialPilot pilot,
      OdometryPoseProvider odometry, Spinner spinner,
      ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps) {
    this.sensor = sensor;
    this.pilot = pilot;
    this.spinner = spinner;
    this.odometry = odometry;
    this.sweeps = sweeps;

  }

  public void run() {
    System.out.println("Subconscious starting");

    float distance;// distance to detected point in m
    float angle;// angle to detected point in radians
    long time;
    Pose pose;

    ArrayList<Measurement> sweep = null;
    Measurement measurement;
    float[] sensorReading = new float[1];

    while (Thread.interrupted() == false) {
      
      // to save my poor processor:
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        synchronized (this) {
          Thread.currentThread().interrupt();
        }
      }
      
      
      // spin the detector back or forth (if we spun one direction the
      // wires would jam):
      spinner.spin();

      // take a sweep of sensor readings:
      sweep = new ArrayList<Measurement>(PROBABLE_MAX_MEASUREMENTS_PER_SWEEP);
      while (spinner.spinning() == true) {

        sensor.fetchSample(sensorReading, 0);

        distance = sensorReading[0];
        angle = spinner.getAngle();
        pose = odometry.getPose();
        time = System.currentTimeMillis();

        measurement = new Measurement(distance, angle, pose, time);
        sweep.add(measurement);
      }

      if (sweep.isEmpty() == false) {
        sweeps.add(sweep);
      }
    }
    System.out.println("Subconscious completed");
  }

}
