package ca.joelathiessen.kaly2.subconscious.sensor

/**
 * Created by joel on 2016-04-23.
 */


interface Kaly2Sensor {
    fun fetchSample(sample: FloatArray, offset: Int)
}