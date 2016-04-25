package ca.joelathiessen.kaly2.featuredetector

import ca.joelathiessen.kaly2.Measurement


interface FeatureDetector {

    fun getFeatures(measurements: List<Measurement>): List<Feature>

}
