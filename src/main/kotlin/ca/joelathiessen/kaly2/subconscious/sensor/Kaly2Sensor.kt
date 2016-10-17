package ca.joelathiessen.kaly2.subconscious.sensor

interface Kaly2Sensor : SensorInfo {
    fun fetchSample(sample: FloatArray, offset: Int)
}