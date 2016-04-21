package ca.joelathiessen.kaly2.featuredetector;

import java.util.ArrayList;
import java.util.List;

import lejos.robotics.geometry.Point;
import ca.joelathiessen.kaly2.subconscious.Measurement;
import ca.joelathiessen.kaly2.featuredetector.Kaly2Feature;

public class SplitAndMerge implements FeatureDetector {

    private static final float THRESHOLD_DIST = 0.1f;

    public List<? extends Kaly2Feature> getFeatures(List<Measurement> measurements) {
        //create a list of points from the measurements
        ArrayList<SplitAndMergeFeature> features = new ArrayList<>(measurements.size());
        for (Measurement mes : measurements) {
            float x = mes.getDistance() * (float) Math.cos(mes.getAngle());
            float y = mes.getDistance() * (float) Math.sin(mes.getAngle());

            features.add(new SplitAndMergeFeature(x, y));
        }

        List<? extends Kaly2Feature> featuresOfInterest = splitAndMerge(features, THRESHOLD_DIST);

        return featuresOfInterest;
    }

    private List<SplitAndMergeFeature> splitAndMerge(List<SplitAndMergeFeature> inputPoints, float epsilon) {
        float distMax = 0;
        int distMaxIndex = 0;
        ArrayList<SplitAndMergeFeature> results = new ArrayList<>(2);

        for (int i = 1; i < inputPoints.size()-1; i++) {
            float dist = distanceFromLineToPoint(inputPoints.get(i), inputPoints.get(0), inputPoints.get(inputPoints.size() - 1));
            if (dist > distMax) {
                distMaxIndex = i;
                distMax = dist;
            }
        }

        if (distMax > epsilon) { // the point is far enough away from our line so keep it
            List<SplitAndMergeFeature> list1 = splitAndMerge(inputPoints.subList(0, distMaxIndex), epsilon);
            List<SplitAndMergeFeature> list2 = splitAndMerge(inputPoints.subList(distMaxIndex, inputPoints.size()), epsilon);

            int subLength = list1.size() - 1;
            results.ensureCapacity(subLength + list2.size());
            results.addAll(list1.subList(0, subLength));
            results.addAll(list2);
        } else { // discard all the points between the start and end points
            inputPoints.get(0).incrDiscardedPointsCount(inputPoints.size()-2);
            results.add(0,inputPoints.get(0));
            results.add(1, inputPoints.get(inputPoints.size() - 1));
        }
        return results;
    }

    private float distanceFromLineToPoint(Kaly2Feature point, Kaly2Feature lineOne, Kaly2Feature lineTwo) {
        float x2MinusX1 = lineTwo.getX() - lineOne.getX();
        float y2MinusY1 = lineTwo.getY() - lineOne.getY();

        float numerator = Math.abs((y2MinusY1 * point.getX()) - (x2MinusX1 * point.getY()) + (lineTwo.getX() * lineOne.getY()) - (lineTwo.getY() * lineOne.getX()));
        float denominator = (float) Math.sqrt(y2MinusY1 * y2MinusY1 + x2MinusX1 * x2MinusX1);

        return numerator / denominator;
    }

}