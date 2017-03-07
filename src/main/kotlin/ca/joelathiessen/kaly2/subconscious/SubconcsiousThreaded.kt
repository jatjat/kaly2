package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.odometry.AccurateSlamOdometry
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2Sensor
import ca.joelathiessen.kaly2.subconscious.sensor.Spinnable
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class SubconsciousThreadedResults(val measurements: ArrayList<Measurement>, val pilotPoses: PilotPoses, val plan: LocalPlan)
class SubconsciousThreaded(private val robotPilot: RobotPilot, private val accurateOdo: AccurateSlamOdometry,
                           private val localPlanner: LocalPlanner, private val localPlannerMaxRot: Float,
                           private val localPlannerMaxDist: Float, private val sensor: Kaly2Sensor,
                           private val spinner: Spinnable, private var globalManeuvers: AtomicReference<List<RobotPose>>,
                           private val resultsQueue: ArrayBlockingQueue<SubconsciousThreadedResults>, private val minMeasTime: Long) {

    private val getGlobalManeuversLock = Any()

    private var subConcCont = true

    fun start() {
        thread {
            while (subConcCont) {
                val startTime = System.currentTimeMillis()

                // get measurements as the robot sees them
                val measurements = ArrayList<Measurement>()
                val mesPose = accurateOdo.getOutputPose()

                spinner.spin()
                while (spinner.spinning) {
                    val sample = FloatArray(2)
                    sensor.fetchSample(sample, 0)
                    measurements.add(Measurement(sample[0], sample[1], mesPose, robotPilot.poses.odoPose,
                            System.currentTimeMillis()))
                }

                // TODO: make this unnecessary by improving LocalPlanner:
                //var gblManeuversToUse = synchronized(gblManeuvers) { gblManeuvers }
                var gblManeuversToUse = globalManeuvers.get()
                synchronized(getGlobalManeuversLock) {
                    if (gblManeuversToUse.isNotEmpty()) {
                        gblManeuversToUse = gblManeuversToUse.subList(1, gblManeuversToUse.size)
                    }
                }

                val plan = localPlanner.makePlan(measurements, mesPose, localPlannerMaxRot, localPlannerMaxDist,
                        gblManeuversToUse)

                robotPilot.execLocalPlan(plan)

                val pilotPoses = robotPilot.poses
                resultsQueue.offer(SubconsciousThreadedResults(measurements, pilotPoses, plan))

                val endTime = System.currentTimeMillis()
                val timeToSleep = minMeasTime - (endTime - startTime)
                if (timeToSleep > 0) {
                    Thread.sleep(timeToSleep)
                }
            }
        }
    }

    fun stop() {
        subConcCont = false
    }
}