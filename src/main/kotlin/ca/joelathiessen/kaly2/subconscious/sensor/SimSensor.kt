package ca.joelathiessen.kaly2.subconscious.sensor

import ca.joelathiessen.kaly2.odometry.RobotPose
import lejos.robotics.geometry.Point
import java.util.*

class SimSensor(var robotPose: RobotPose, var sensorAng: Double = 0.0, val osGrid: Array<Array<Point?>>,
                val gridWidth: Int, val gridHeight: Int, val maxSensorRange: Double,
                val sensorDistStdev: Double = 0.0, val sensorAngStdev: Double = 0.0): Kaly2Sensor {
    val DIST_INCR = 0.5
    private val random = Random(0)

    override fun fetchSample(sample: FloatArray, offset: Int) {
        var cont = true
        val sinConst = Math.sin(sensorAng)
        val cosConst = Math.cos(sensorAng)
        var dist = 0.0

        while (dist < maxSensorRange && cont) {
            val y = (robotPose.y + (sinConst * dist)).toInt()
            val x = (robotPose.x + (cosConst * dist)).toInt()

            if(x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                val next = osGrid[x][y]

                if (next != null) {
                    sample[offset] = robotPose.distanceTo(next) + (random.nextGaussian() * sensorDistStdev).toFloat()

                    // assume angle is obtained using a reference frame set by a compass:
                    sample[offset + 1] = (Math.atan2(next.y - robotPose.y.toDouble(), next.x - robotPose.x.toDouble())
                            + (random.nextGaussian() * sensorAngStdev)).toFloat()
                    cont = false
                }
                dist += DIST_INCR
            } else {
                cont = false
            }
        }
    }
}