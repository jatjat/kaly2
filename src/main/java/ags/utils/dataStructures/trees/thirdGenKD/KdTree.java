package ags.utils.dataStructures.trees.thirdGenKD;

import ags.utils.dataStructures.BinaryHeap;
import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.MinHeap;

/**
 *
 */
public class KdTree<T> extends KdNode<T> {
    public KdTree(int dimensions) {
        this(dimensions, 24);
    }

    public KdTree(int dimensions, int bucketCapacity) {
        super(dimensions, bucketCapacity);
    }

    /* -------- Joel's constructor -------- */
    private KdTree(int dimensions, int bucketCapacity, int size, double[][] points, Object[] data,
                   KdNode<T> left, KdNode<T> right, int splitDimension, double splitValue,
                   double[] minBound, double[] maxBound, boolean singlePoint) {
        super(dimensions, bucketCapacity, size, points, data, left, right, splitDimension,
                splitValue, minBound, maxBound, singlePoint);
    }

    /* -------- Joel's copy on insert -------- */
    public KdTree<T> addPointAsCopy(double[] point, T value) {
        double[][] newPoints = null;
        if (points != null) {
            newPoints = points.clone();
        }

        Object[] newData = null;
        if (data != null) {
            newData = data.clone();
        }

        KdTree<T> newTree = new KdTree<T>(dimensions, bucketCapacity, size, newPoints, newData,
                null, null, splitDimension, splitValue, minBound, maxBound, singlePoint);
        addPointAsCopy(newTree, point, value);

        return newTree;
    }

    public NearestNeighborIterator<T> getNearestNeighborIterator(double[] searchPoint, int maxPointsReturned, DistanceFunction distanceFunction) {
        return new NearestNeighborIterator<T>(this, searchPoint, maxPointsReturned, distanceFunction);
    }

    public MaxHeap<T> findNearestNeighbors(double[] searchPoint, int maxPointsReturned, DistanceFunction distanceFunction) {
        BinaryHeap.Min<KdNode<T>> pendingPaths = new BinaryHeap.Min<KdNode<T>>();
        BinaryHeap.Max<T> evaluatedPoints = new BinaryHeap.Max<T>();
        int pointsRemaining = Math.min(maxPointsReturned, size());
        pendingPaths.offer(0, this);

        while (pendingPaths.size() > 0 && (evaluatedPoints.size() < pointsRemaining || (pendingPaths.getMinKey() < evaluatedPoints.getMaxKey()))) {
            nearestNeighborSearchStep(pendingPaths, evaluatedPoints, pointsRemaining, distanceFunction, searchPoint);
        }

        return evaluatedPoints;
    }

    @SuppressWarnings("unchecked")
    protected static <T> void nearestNeighborSearchStep (
            MinHeap<KdNode<T>> pendingPaths, MaxHeap<T> evaluatedPoints, int desiredPoints,
            DistanceFunction distanceFunction, double[] searchPoint) {
        // If there are pending paths possibly closer than the nearest evaluated point, check it out
        KdNode<T> cursor = pendingPaths.getMin();
        pendingPaths.removeMin();

        // Descend the tree, recording paths not taken
        while (!cursor.isLeaf()) {
            KdNode<T> pathNotTaken;
            if (searchPoint[cursor.splitDimension] > cursor.splitValue) {
                pathNotTaken = cursor.left;
                cursor = cursor.right;
            }
            else {
                pathNotTaken = cursor.right;
                cursor = cursor.left;
            }
            double otherDistance = distanceFunction.distanceToRect(searchPoint, pathNotTaken.minBound, pathNotTaken.maxBound);
            // Only add a path if we either need more points or it's closer than furthest point on list so far
            if (evaluatedPoints.size() < desiredPoints || otherDistance <= evaluatedPoints.getMaxKey()) {
                pendingPaths.offer(otherDistance, pathNotTaken);
            }
        }

        if (cursor.singlePoint) {
            double nodeDistance = distanceFunction.distance(cursor.points[0], searchPoint);
            // Only add a point if either need more points or it's closer than furthest on list so far
            if (evaluatedPoints.size() < desiredPoints || nodeDistance <= evaluatedPoints.getMaxKey()) {
                for (int i = 0; i < cursor.size(); i++) {
                    T value = (T) cursor.data[i];

                    // If we don't need any more, replace max
                    if (evaluatedPoints.size() == desiredPoints) {
                        evaluatedPoints.replaceMax(nodeDistance, value);
                    } else {
                        evaluatedPoints.offer(nodeDistance, value);
                    }
                }
            }
        } else {
            // Add the points at the cursor
            for (int i = 0; i < cursor.size(); i++) {
                double[] point = cursor.points[i];
                T value = (T) cursor.data[i];
                double distance = distanceFunction.distance(point, searchPoint);
                // Only add a point if either need more points or it's closer than furthest on list so far
                if (evaluatedPoints.size() < desiredPoints) {
                    evaluatedPoints.offer(distance, value);
                } else if (distance < evaluatedPoints.getMaxKey()) {
                    evaluatedPoints.replaceMax(distance, value);
                }
            }
        }
    }
}
