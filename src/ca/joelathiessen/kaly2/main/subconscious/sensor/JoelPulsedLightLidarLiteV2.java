package ca.joelathiessen.kaly2.main.subconscious.sensor;

import java.util.ArrayList;

import ca.joelathiessen.kaly2.main.subconscious.Measurement;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.I2CSensor;
import lejos.robotics.RegulatedMotor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.sensor.EV3IRSensor;

public class JoelPulsedLightLidarLiteV2 extends I2CSensor {

	public JoelPulsedLightLidarLiteV2(Port port) {
		super(port);
	}

	// TODO: install the sensor!
	public void fetchSample(float[] sample, int offset) {

	}
}
