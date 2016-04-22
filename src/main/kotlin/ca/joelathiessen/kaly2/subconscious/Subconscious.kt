package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2PulsedLightLidarLiteV2
import ca.joelathiessen.kaly2.subconscious.sensor.Spinner
import lejos.robotics.localization.OdometryPoseProvider
import lejos.robotics.navigation.DifferentialPilot
import lejos.robotics.navigation.Pose

import java.util.ArrayList
import java.util.concurrent.ConcurrentLinkedQueue

class Subconscious(private val sensor: Kaly2PulsedLightLidarLiteV2, private val pilot: DifferentialPilot,
                   private val odometry: OdometryPoseProvider, private val spinner: Spinner,
                   private val sweeps: ConcurrentLinkedQueue<ArrayList<Measurement>>) : Runnable {

    // probable maximum number of measurements we will get per
    // 360 degree spin of the distance detector:
    final val PROBABLE_MAX_MEASUREMENTS_PER_SWEEP = 360

    override fun run() {
        println("Subconscious starting")

        var distance: Float// distance to detected point in m
        var angle: Float// angle to detected point in radians
        var time: Long
        var pose: Pose

        var sweep: ArrayList<Measurement>? = null
        var measurement: Measurement
        val sensorReading = FloatArray(1)

        while (Thread.interrupted() == false) {

            // to save my poor processor:
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                synchronized (this) {
                    Thread.currentThread().interrupt()
                }
            }


            // spin the detector back or forth (if we spun one direction the
            // wires would jam):
            spinner.spin()

            // take a sweep of sensor readings:
            sweep = ArrayList<Measurement>(PROBABLE_MAX_MEASUREMENTS_PER_SWEEP)
            while (spinner.spinning() == true) {

                sensor.fetchSample(sensorReading, 0)

                distance = sensorReading[0]
                angle = spinner.angle
                pose = odometry.pose
                time = System.currentTimeMillis()

                measurement = Measurement(distance, angle, pose, time)
                sweep.add(measurement)
            }

            if (sweep.isEmpty() == false) {
                sweeps.add(sweep)
            }
        }
        println("Subconscious completed")
    }

    /*companion object {

        // probable maximum number of measurements we will get per
        // 350 degree spin of the distance detector
        //val PROBABLE_MAX_MEASUREMENTS_PER_SWEEP = 360
    }*/

}
