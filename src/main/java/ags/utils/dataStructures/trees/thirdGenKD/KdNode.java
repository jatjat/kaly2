package ags.utils.dataStructures.trees.thirdGenKD;

import java.util.Arrays;

/**
 *
 */
class KdNode<T> {
    // All types
    protected int dimensions;
    protected int bucketCapacity;
    protected int size;

    // Leaf only
    protected double[][] points;
    protected Object[] data;

    // Stem only
    protected KdNode<T> left, right;
    protected int splitDimension;
    protected double splitValue;

    // Bounds
    protected double[] minBound, maxBound;
    protected boolean singlePoint;

    protected KdNode(int dimensions, int bucketCapacity) {
        // Init base
        this.dimensions = dimensions;
        this.bucketCapacity = bucketCapacity;
        this.size = 0;
        this.singlePoint = true;

        // Init leaf elements
        this.points = new double[bucketCapacity+1][];
        this.data = new Object[bucketCapacity+1];
    }

    /* -------- Joel's constructor -------- */
    protected KdNode(KdNode<T> node) {
        dimensions = node.dimensions;
        bucketCapacity = node.bucketCapacity;
        size = node.size;
        points = node.points != null ? node.points.clone() : null;
        data = node.data != null ? node.data.clone() : null;
        left = node.left;
        right = node.right;
        splitDimension = node.splitDimension;
        splitValue = node.splitValue;
        minBound = node.minBound != null ? node.minBound.clone() : null;
        maxBound = node.maxBound != null ? node.maxBound.clone() : null;
        singlePoint = node.singlePoint;
    }

    /* -------- SIMPLE GETTERS -------- */

    public int size() {
        return size;
    }

    public boolean isLeaf() {
        return points != null;
    }

    /* -------- OPERATIONS -------- */

    public void addPoint(double[] point, T value) {
        KdNode<T> cursor = this;
        while (!cursor.isLeaf()) {
            cursor.extendBounds(point);
            cursor.size++;
            if (point[cursor.splitDimension] > cursor.splitValue) {
                cursor = cursor.right;
            }
            else {
                cursor = cursor.left;
            }
        }
        cursor.addLeafPoint(point, value);
    }

    /* -------- Joel's copy on insert -------- */
    protected void addPointAsCopy(KdNode<T> newRootNode, double[] point, T value) {
        KdNode<T> cursor = this;
        KdNode<T> parentCursor = null;
        KdNode<T> newCursor = newRootNode;

        while (!cursor.isLeaf()) {
            newCursor.extendBounds(point);
            newCursor.size++;
            if (point[cursor.splitDimension] > cursor.splitValue) {
                newCursor.left = cursor.left;
                newCursor.right = null;
                cursor = cursor.right;
            } else {
                newCursor.right = cursor.right;
                newCursor.left = null;
                cursor = cursor.left;
            }
            addChildToParent(newCursor, parentCursor);
            parentCursor = newCursor;

            newCursor = new KdNode<T>(cursor);
        }

        addChildToParent(newCursor, parentCursor);
        newCursor.addLeafPoint(point, value);
    }

    /* -------- Joel's copy on remove -------- */
    protected void removePointAsCopy(KdNode<T> newRootNode, double[] point, T value) {
        KdNode<T> cursor = this;
        KdNode<T> parentCursor = null;
        KdNode<T> newCursor = newRootNode;

        while (!cursor.isLeaf()) {
            newCursor.extendBounds(point);
            newCursor.size--;
            if (point[cursor.splitDimension] > cursor.splitValue) {
                newCursor.left = cursor.left;
                newCursor.right = null;
                cursor = cursor.right;
            } else {
                newCursor.right = cursor.right;
                newCursor.left = null;
                cursor = cursor.left;
            }
            addChildToParent(newCursor, parentCursor);
            parentCursor = newCursor;

            newCursor = new KdNode<>(cursor);
        }

        addChildToParent(newCursor, parentCursor);
        newCursor.removeLeafPoint(point, value);
    }

    /* -------- INTERNAL OPERATIONS -------- */

    /* -------- Joel's add child to parent -------- */
    private void addChildToParent(KdNode<T> childCursor, KdNode<T> parentCursor) {
        if (parentCursor != null) {
            if (parentCursor.left == null) {
                parentCursor.left = childCursor;
            } else if (parentCursor.right == null) {
                parentCursor.right = childCursor;
            } else {
                throw new IllegalStateException("Parent KdNode does not have a null child variable to add a child to");
            }
        }
    }

    /* -------- Joel's remove point from leaf -------- */
    // Any point can be removed from the leaf without losing correctness but
    // there's a tradeoff between a very small increase in search performance if
    // the split values and split dimensions are then recalculated vs. increased
    // delete performance if they are not. Here they are not.
    public void removeLeafPoint(double[] point, T value) {
        for (int i = 0; i < size; i++) { // points are stored unordered
            if (value == data[i]) {
                if (size > 1) {
                    for (int j = i + 1; j < size; j++) {
                        points[j - 1] = points[j];
                        data[j - 1] = data[j];
                    }
                } else {
                    if (size == 1) {
                        minBound = Arrays.copyOf(point, dimensions);
                        maxBound = Arrays.copyOf(point, dimensions);
                    }
                    singlePoint = true;
                }
                size--;
                break;
            }
        }
    }

    public void addLeafPoint(double[] point, T value) {
        // Add the data point
        points[size] = point;
        data[size] = value;
        extendBounds(point);
        size++;

        if (size == points.length - 1) {
            // If the node is getting too large
            if (calculateSplit()) {
                // If the node successfully had it's split value calculated, split node
                splitLeafNode();
            } else {
                // If the node could not be split, enlarge node
                increaseLeafCapacity();
            }
        }
    }

    private boolean checkBounds(double[] point) {
        for (int i = 0; i < dimensions; i++) {
            if (point[i] > maxBound[i]) return false;
            if (point[i] < minBound[i]) return false;
        }
        return true;
    }

    private void extendBounds(double[] point) {
        if (minBound == null) {
            minBound = Arrays.copyOf(point, dimensions);
            maxBound = Arrays.copyOf(point, dimensions);
            return;
        }

        for (int i = 0; i < dimensions; i++) {
            if (Double.isNaN(point[i])) {
                if (!Double.isNaN(minBound[i]) || !Double.isNaN(maxBound[i])) {
                    singlePoint = false;
                }
                minBound[i] = Double.NaN;
                maxBound[i] = Double.NaN;
            }
            else if (minBound[i] > point[i]) {
                minBound[i] = point[i];
                singlePoint = false;
            }
            else if (maxBound[i] < point[i]) {
                maxBound[i] = point[i];
                singlePoint = false;
            }
        }
    }

    private void increaseLeafCapacity() {
        points = Arrays.copyOf(points, points.length*2);
        data = Arrays.copyOf(data, data.length*2);
    }

    private boolean calculateSplit() {
        if (singlePoint) return false;

        double width = 0;
        for (int i = 0; i < dimensions; i++) {
            double dwidth = (maxBound[i] - minBound[i]);
            if (Double.isNaN(dwidth)) dwidth = 0;
            if (dwidth > width) {
                splitDimension = i;
                width = dwidth;
            }
        }

        if (width == 0) {
            return false;
        }

        // Start the split in the middle of the variance
        splitValue = (minBound[splitDimension] + maxBound[splitDimension]) * 0.5;

        // Never split on infinity or NaN
        if (splitValue == Double.POSITIVE_INFINITY) {
            splitValue = Double.MAX_VALUE;
        }
        else if (splitValue == Double.NEGATIVE_INFINITY) {
            splitValue = -Double.MAX_VALUE;
        }

        // Don't let the split value be the same as the upper value as
        // can happen due to rounding errors!
        if (splitValue == maxBound[splitDimension]) {
            splitValue = minBound[splitDimension];
        }

        // Success
        return true;
    }

    @SuppressWarnings("unchecked")
    private void splitLeafNode() {
        right = new KdNode<T>(dimensions, bucketCapacity);
        left = new KdNode<T>(dimensions, bucketCapacity);

        // Move locations into children
        for (int i = 0; i < size; i++) {
            double[] oldLocation = points[i];
            Object oldData = data[i];
            if (oldLocation[splitDimension] > splitValue) {
                right.addLeafPoint(oldLocation, (T) oldData);
            }
            else {
                left.addLeafPoint(oldLocation, (T) oldData);
            }
        }

        points = null;
        data = null;
    }
}
