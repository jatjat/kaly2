package ca.joelathiessen.kaly2.core.subconscious.sensor

import lejos.hardware.port.Port
import lejos.hardware.sensor.I2CSensor

class Kaly2PulsedLightLidarLiteV2(port: Port) : I2CSensor(port), Kaly2Sensor {

    // TODO: install the sensor!
    override fun fetchSample(sample: FloatArray, offset: Int) {
    }
}
