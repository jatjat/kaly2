package ca.joelathiessen.kaly2.featuredetector;

import ca.joelathiessen.kaly2.subconscious.Measurement;

import java.util.List;


public interface FeatureDetector {

    List<? extends Kaly2Feature> getFeatures(List<Measurement> measurements);

}
