package ca.joelathiessen.kaly2.featuredetector;

import java.util.List;

import ca.joelathiessen.kaly2.subconscious.Measurement;


public interface FeatureDetector {

  List<? extends Kaly2Feature> getFeatures(List<Measurement> measurements);

}
