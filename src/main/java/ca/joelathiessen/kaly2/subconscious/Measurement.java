package ca.joelathiessen.kaly2.subconscious;

import lejos.robotics.navigation.Pose;

public class Measurement {

    private float distance;
    private float angle;
    private Pose pose;
    private long time;

    public Measurement(float distance, float angle, Pose pose, long time) {
        this.distance = distance;
        this.angle = angle;
        this.pose = pose;
        this.time = time;
    }

    public float getDistance() {
        return this.distance;
    }

    public float getAngle() {
        return this.angle;
    }

    public Pose getPose() {
        return this.pose;
    }

    public long getTime() {
        return this.time;
    }

}
