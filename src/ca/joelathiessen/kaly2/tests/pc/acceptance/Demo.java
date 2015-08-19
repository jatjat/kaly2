package ca.joelathiessen.kaly2.tests.pc.acceptance;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mockito.Mockito;

import ca.joelathiessen.kaly2.main.Robot;
import ca.joelathiessen.kaly2.main.subconscious.Measurement;
import ca.joelathiessen.kaly2.main.subconscious.Subconscious;
import ca.joelathiessen.kaly2.main.subconscious.sensor.JoelPulsedLightLidarLiteV2;
import ca.joelathiessen.kaly2.main.subconscious.sensor.Spinner;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.I2CSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;

public class Demo {

  public static void main(String[] args) {
    System.out.println("Running kaly2 on a PC, attempting to mock only access to the hardware");

    JoelPulsedLightLidarLiteV2 sensor = Mockito.mock(JoelPulsedLightLidarLiteV2.class);
    DifferentialPilot pilot = Mockito.mock(DifferentialPilot.class);
    OdometryPoseProvider odometry = Mockito.mock(OdometryPoseProvider.class);
    EV3LargeRegulatedMotor spinMotor = Mockito.mock(EV3LargeRegulatedMotor.class);
    Spinner spinner = new Spinner(spinMotor);
    ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps =
        new ConcurrentLinkedQueue<ArrayList<Measurement>>();

    Subconscious sub = new Subconscious(sensor, pilot, odometry, spinner, sweeps);
    Robot robot = new Robot(sub);
    robot.start();

    System.out.println("Demo completed");
  }

}
