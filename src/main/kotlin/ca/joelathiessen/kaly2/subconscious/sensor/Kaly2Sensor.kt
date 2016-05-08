package ca.joelathiessen.kaly2.subconscious.sensor

interface Kaly2Sensor {
    fun fetchSample(sample: FloatArray, offset: Int)
}