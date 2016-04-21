package ca.joelathiessen.kaly2.featuredetector;

import java.util.ArrayList;
import java.util.List;

import lejos.robotics.geometry.Point;
import ca.joelathiessen.kaly2.subconscious.Measurement;

public class SplitMergeFeatureDetector implements FeatureDetector {

    private static final float THRESHOLD_DIST = 0.1f;

    public List<Kaly2Feature> getFeatures(List<Measurement> measurements) {
        //create a list of points from the measurements
        ArrayList<Point> points = new ArrayList<Point>(measurements.size());
        for (Measurement mes : measurements) {
            float x = mes.getAngle() * (float) Math.cos(mes.getDistance());
            float y = mes.getAngle() * (float) Math.sin(mes.getDistance());

            Point pnt = new Point(x, y);
            points.add(pnt);
        }

        List<Point> pointsOfInterest = splitAndMerge(points, THRESHOLD_DIST);

        ArrayList<Kaly2Feature> features = null;

        return features;
    }

    private List<Point> splitAndMerge(List<Point> inputPoints, float epsilon) {
        float distMax = 0;
        int distMaxIndex = 0;
        ArrayList<Point> results = new ArrayList<Point>(2);

        for (int i = 2; i < inputPoints.size(); i++) {
            float dist = distanceFromLineToPoint(inputPoints.get(i), inputPoints.get(0), inputPoints.get(inputPoints.size() - 1));
            if (dist > distMax) {
                distMaxIndex = i;
                distMax = dist;
            }
        }

        if (distMax > epsilon) {
            List<Point> list1 = splitAndMerge(inputPoints.subList(0, distMaxIndex), epsilon);
            List<Point> list2 = splitAndMerge(inputPoints.subList(distMaxIndex, inputPoints.size()), epsilon);

            int subLength = list1.size() - 1;
            results.ensureCapacity(subLength + list2.size());
            results.addAll(list1.subList(0, subLength));
            results.addAll(list2);

        } else {
            results.set(0, inputPoints.get(0));
            results.set(1, inputPoints.get(inputPoints.size() - 1));
        }

        return results;
    }

    private float distanceFromLineToPoint(Point point, Point lineOne, Point lineTwo) {
        float x2MinusX1 = lineTwo.x - lineOne.x;
        float y2MinusY1 = lineTwo.y - lineOne.y;

        float numerator = Math.abs((y2MinusY1 * point.x) - (x2MinusX1 * point.y) + (lineTwo.x * lineOne.y) - (lineTwo.y * lineOne.x));
        float denominator = (float) Math.sqrt(y2MinusY1 * y2MinusY1 + x2MinusX1 * x2MinusX1);

        return numerator / denominator;
    }

}