package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.odometry.AccurateSlamOdometry
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2Sensor
import ca.joelathiessen.kaly2.subconscious.sensor.Spinnable
import java.util.*

class SubconsciousActedResults(val measurements: ArrayList<Measurement>, val pilotPoses: PilotPoses, val plan: LocalPlan)
class SubconsciousActed(private val robotPilot: RobotPilot, private val accurateOdo: AccurateSlamOdometry,
                        private val localPlanner: LocalPlanner, private val localPlannerMaxRot: Float,
                        private val localPlannerMaxDist: Float, private val sensor: Kaly2Sensor,
                        private val spinner: Spinnable, private var globalManeuvers: List<RobotPose>,
                        private val minMeasTime: Long) {

    fun iterate(newGlobalManeuvers: List<RobotPose> = globalManeuvers): SubconsciousActedResults {
        val startTime = System.currentTimeMillis()
        var nextManeuvers = newGlobalManeuvers
        if(globalManeuvers != nextManeuvers) {
            // TODO: make this unnecessary by improving LocalPlanner:
            if (nextManeuvers.isNotEmpty()) {
                nextManeuvers = nextManeuvers.subList(1, nextManeuvers.size)
            }
        }
        globalManeuvers = nextManeuvers

        val pilotPoses = robotPilot.poses

        val measurements = ArrayList<Measurement>()
        val mesPose = accurateOdo.getOutputPose()
        spinner.spin()
        while (spinner.spinning) {
            val sample = FloatArray(2)
            sensor.fetchSample(sample, 0)
            measurements.add(Measurement(sample[0], sample[1], mesPose, robotPilot.poses.odoPose,
                    System.currentTimeMillis()))
        }

        val plan = localPlanner.makePlan(measurements, mesPose, localPlannerMaxRot, localPlannerMaxDist,
                globalManeuvers)

        robotPilot.execLocalPlan(plan)

        val endTime = System.currentTimeMillis()
        val timeToSleep = minMeasTime - (endTime - startTime)
        if (timeToSleep > 0) {
            Thread.sleep(timeToSleep)
        }

        return SubconsciousActedResults(measurements, pilotPoses, plan)
    }
}