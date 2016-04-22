package ca.joelathiessen.kaly2.featuredetector

import ca.joelathiessen.kaly2.subconscious.Measurement


interface FeatureDetector {

    fun getFeatures(measurements: List<Measurement>): List<Kaly2Feature>

}
