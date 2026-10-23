package ca.joelathiessen.kaly2.core.featuredetector

import ca.joelathiessen.kaly2.core.Measurement

interface FeatureDetector {
    fun getFeatures(measurements: List<Measurement>): List<Feature>
}
