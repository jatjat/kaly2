package ca.joelathiessen.kaly2.tests.pc.acceptance;

import ca.joelathiessen.kaly2.Commander;
import ca.joelathiessen.kaly2.Robot;
import ca.joelathiessen.kaly2.sensor.Measurement;
import ca.joelathiessen.kaly2.subconscious.Subconscious;
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2PulsedLightLidarLiteV2;
import ca.joelathiessen.kaly2.subconscious.sensor.Spinner;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Demo {

    public static void main(String[] args) {
        System.out.println("Running kaly2 on a PC, attempting to mock only access to the hardware");

        Kaly2PulsedLightLidarLiteV2 sensor = Mockito.mock(Kaly2PulsedLightLidarLiteV2.class);
        DifferentialPilot pilot = Mockito.mock(DifferentialPilot.class);
        OdometryPoseProvider odometry = Mockito.mock(OdometryPoseProvider.class);
        EV3LargeRegulatedMotor spinMotor = Mockito.mock(EV3LargeRegulatedMotor.class);
        Spinner spinner = new Spinner(spinMotor);
        ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps =
                new ConcurrentLinkedQueue<ArrayList<Measurement>>();

        Subconscious sub = new Subconscious(sensor, pilot, odometry, spinner, sweeps);
        Robot robot = new Robot(sub, sweeps);
        Commander commander = new Commander(robot);
        commander.takeCommands();


        System.out.println("Demo completed");
    }

}
