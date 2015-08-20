package ca.joelathiessen.kaly2.main.subconscious.sensor;

import lejos.robotics.RegulatedMotor;

public class Spinner {
  private static final int DETECTOR_SPEED_DEGS_SEC = 100;

  private RegulatedMotor motor;

  public static final int MAX_DETECTOR_ANGLE_DEG = 180;
  public static final int MIN_DETECTOR_ANGLE_DEG = -180;

  private int maxDetectorAngleDeg;
  private int minDetectorAngleDeg;

  private boolean turnClockwise = true;
  private boolean spin = false;

  public Spinner(RegulatedMotor motor, int maxDetectorAngleDeg, int minDetectorAngleDeg) {

    this.motor = motor;
    this.maxDetectorAngleDeg = maxDetectorAngleDeg;
    this.minDetectorAngleDeg = minDetectorAngleDeg;
    motor.setSpeed(DETECTOR_SPEED_DEGS_SEC);
  }

  public Spinner(RegulatedMotor motor) {

    this.motor = motor;
    this.maxDetectorAngleDeg = MAX_DETECTOR_ANGLE_DEG;
    this.minDetectorAngleDeg = MIN_DETECTOR_ANGLE_DEG;
    motor.setSpeed(DETECTOR_SPEED_DEGS_SEC);
  }

  public void spin() {

    // spin the detector back and forth (if we spun one direction
    // the wires would jam):
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
