package ca.joelathiessen.kaly2.featuredetector;

import java.util.List;

import ca.joelathiessen.kaly2.subconscious.Measurement;


public interface FeatureDetector {

  public List<Kaly2Feature> getFeatures(List<Measurement> measurements);

}
