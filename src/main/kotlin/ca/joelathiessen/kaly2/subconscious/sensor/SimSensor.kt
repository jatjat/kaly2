package ca.joelathiessen.kaly2.subconscious.sensor

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.FloatRandom
import lejos.robotics.geometry.Point

class SimSensor(private val osGrid: Array<Array<Point?>>,
                private val gridWidth: Int, private val gridHeight: Int, private val maxSensorRange: Float,
                private val sensorDistStdev: Float = 0.0f, private val sensorAngStdev: Float = 0.0f,
                private val spinner: SimSpinner, private val getRealRobotPose: () -> RobotPose): Kaly2Sensor {
    val DIST_INCR = 0.5f
    private val random = FloatRandom(0)

    override fun fetchSample(sample: FloatArray, offset: Int) {
        var cont = true
        val sinConst = FloatMath.sin(spinner.angle)
        val cosConst = FloatMath.cos(spinner.angle)
        var dist = 0.0f
        val robotPose = getRealRobotPose()

        while (dist < maxSensorRange && cont) {
            val y = (robotPose.y + (sinConst * dist)).toInt()
            val x = (robotPose.x + (cosConst * dist)).toInt()

            if(x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                val next = osGrid[x][y]

                if (next != null) {
                    sample[offset] = robotPose.distanceTo(next) + (random.nextGaussian() * sensorDistStdev)

                    // assume angle is obtained using a reference frame set by a compass:
                    sample[offset + 1] = (FloatMath.atan2(next.y - robotPose.y, next.x - robotPose.x)
                            + (random.nextGaussian() * sensorAngStdev))
                    cont = false
                }
                dist += DIST_INCR
            } else {
                cont = false
            }
        }
        spinner.incr()
    }
}