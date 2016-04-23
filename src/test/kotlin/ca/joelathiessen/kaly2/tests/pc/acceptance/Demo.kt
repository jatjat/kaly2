package ca.joelathiessen.kaly2.tests.pc.acceptance

import ca.joelathiessen.kaly2.Commander
import ca.joelathiessen.kaly2.Robot
import ca.joelathiessen.kaly2.subconscious.Measurement
import ca.joelathiessen.kaly2.subconscious.Subconscious
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2PulsedLightLidarLiteV2
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2Sensor
import ca.joelathiessen.kaly2.subconscious.sensor.Spinner
import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.robotics.localization.OdometryPoseProvider
import lejos.robotics.navigation.DifferentialPilot
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

import java.util.ArrayList
import java.util.concurrent.ConcurrentLinkedQueue


object Demo {

    @JvmStatic fun main(args: Array<String>) {
        println("Running kaly2 on a PC, attempting to mock only access to the hardware")

        val sensor = Mockito.mock(Kaly2Sensor::class.java)
        val pilot = Mockito.mock(DifferentialPilot::class.java)
        val odometry = Mockito.mock(OdometryPoseProvider::class.java)
        val spinMotor = Mockito.mock(EV3LargeRegulatedMotor::class.java)
        val spinner = Spinner(spinMotor)
        val sweeps = ConcurrentLinkedQueue<ArrayList<Measurement>>()

        val sub = Subconscious(sensor, pilot, odometry, spinner, sweeps)
        val robot = Robot(sub, sweeps)
        val commander = Commander(robot)
        commander.takeCommands()


        println("Demo completed")
    }

}
