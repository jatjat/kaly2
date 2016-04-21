package ca.joelathiessen.kaly2.featuredetector;

public class SplitAndMergeFeature implements Kaly2Feature {
  private float x;
  private float y;
  private float stDev;

  private int discardedPoints;// the number of points along the line starting at this point that were discarded during the split and merge

  public SplitAndMergeFeature(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getStdDev() {
    return stDev;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  void incrDiscardedPointsCount(int inc) {
    discardedPoints += Math.max(inc, 0);
  }
}
