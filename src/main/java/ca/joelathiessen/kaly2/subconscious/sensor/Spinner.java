package ca.joelathiessen.kaly2.subconscious.sensor;

import lejos.robotics.RegulatedMotor;

public class Spinner {

  private RegulatedMotor motor;

  public static final int DETECTOR_SPEED_DEGS_SEC = 100;

  public static final int DEFAULT_MAX_DETECTOR_ANGLE_DEG = 180;
  public static final int DEFAULT_MIN_DETECTOR_ANGLE_DEG = -180;

  private int maxDetectorAngleDeg;
  private int minDetectorAngleDeg;

  private boolean turnClockwise = true;

  public Spinner(RegulatedMotor motor, int maxDetectorAngleDeg, int minDetectorAngleDeg) {

    this.motor = motor;
    this.maxDetectorAngleDeg = maxDetectorAngleDeg;
    this.minDetectorAngleDeg = minDetectorAngleDeg;
    motor.setSpeed(DETECTOR_SPEED_DEGS_SEC);
  }

  public Spinner(RegulatedMotor motor) {

    this.motor = motor;
    this.maxDetectorAngleDeg = DEFAULT_MAX_DETECTOR_ANGLE_DEG;
    this.minDetectorAngleDeg = DEFAULT_MIN_DETECTOR_ANGLE_DEG;
    motor.setSpeed(DETECTOR_SPEED_DEGS_SEC);
  }

  public void spin() {

    // spin the detector back and forth (if we spun one direction,
    // for the current sensor configuration the wires would jam):
    if (turnClockwise == true) {
      motor.rotateTo(maxDetectorAngleDeg, true);
      turnClockwise = false;
    } else {
      motor.rotateTo(minDetectorAngleDeg, true);
      turnClockwise = true;
    }
  }

  public boolean spinning() {
    return motor.isMoving();
  }

  public float getAngle() {
    return (float) Math.toRadians(motor.getTachoCount());
  }

  public boolean turningClockwise() {
    return turnClockwise;
  }
}
