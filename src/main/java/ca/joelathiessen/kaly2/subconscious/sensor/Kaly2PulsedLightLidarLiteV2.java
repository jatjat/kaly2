package ca.joelathiessen.kaly2.subconscious.sensor;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.I2CSensor;

public class Kaly2PulsedLightLidarLiteV2 extends I2CSensor {

    public Kaly2PulsedLightLidarLiteV2(Port port) {
        super(port);
    }

    // TODO: install the sensor!
    public void fetchSample(float[] sample, int offset) {

    }
}
