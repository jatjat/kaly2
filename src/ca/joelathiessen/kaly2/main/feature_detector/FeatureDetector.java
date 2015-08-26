package ca.joelathiessen.kaly2.main.feature_detector;

import java.util.List;

import ca.joelathiessen.kaly2.main.subconscious.Measurement;


public interface FeatureDetector {

  public List<Kaly2Feature> getFeatures(List<Measurement> measurements);

}
