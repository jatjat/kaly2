package ca.joelathiessen.kaly2

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.FeatureDetector
import ca.joelathiessen.kaly2.map.GlobalMap
import ca.joelathiessen.kaly2.map.MapTree
import ca.joelathiessen.kaly2.odometry.AccurateSlamOdometry
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.persistence.RobotStorage
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.server.SlamSettings
import ca.joelathiessen.kaly2.server.messages.RTRobotMsg
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.Slam
import ca.joelathiessen.kaly2.subconscious.SubconsciousActedResults
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.util.ArrayList
import java.util.Random

data class RobotCoreActedResults(
    val timestamp: Long,
    val features: List<Feature>,
    val obstacles: List<Point>,
    val slamPose: RobotPose,
    val maneuvers: List<RobotPose>,
    val globalPlannerPaths: List<PathSegmentInfo>,
    val subconcResults: SubconsciousActedResults,
    val particlePoses: List<Pose>,
    val numItrs: Long
) : RTRobotMsg {
    override val MSG_TYPE: String = "RobotCoreActedResults"
}

class RobotCoreActed(
    private val initialGoal: RobotPose,
    private val accurateOdo: AccurateSlamOdometry,
    private val slam: Slam,
    private val featureDetector: FeatureDetector,
    private val map: GlobalMap,
    private val robotStorage: RobotStorage
) {

    private val REQ_MAN_INTERVAL = 5
    private val UPDATE_PLAN_START_POSE_INTERVAL = 10
    private val UPDATE_PLAN_END_POSE_INTERVAL = 20
    private val MAX_PLAN_DIST = 4096
    private val random = Random(0)
    private var numItrs = 0L
    private var currentPlanItrs = 0

    var features: List<Feature> = ArrayList()
        private set

    var maneuvers: List<RobotPose> = ArrayList()

    var paths: List<PathSegmentInfo> = ArrayList()
        private set

    var subconcResults: SubconsciousActedResults? = null
        private set

    var particlePoses: List<Pose> = ArrayList()
        private set

    var reqPlannerManeuvers: () -> Unit = {}
    var sendPlannerManeuversToLocalPlanner: (List<RobotPose>) -> Unit = {}
    var planFrom = { _: RobotPose, _: MapTree -> Unit }
    var planTo: (RobotPose) -> Unit = {}

    init {
        val measurements = robotStorage.getMeasurements()
        measurements.forEach {
            map.incorporateMeasurements(it.measurements, it.slamPose)
            numItrs++
        }
    }

    fun onManeuverResults(newManeuvers: List<RobotPose>) {
        maneuvers = newManeuvers
        sendPlannerManeuversToLocalPlanner(maneuvers)
    }

    fun onPaths(newPaths: List<PathSegmentInfo>) {
        paths = newPaths
    }

    fun onSettings(newSettings: SlamSettings): SlamSettings {
        if (slam is FastSLAM) {
            slam.changeAngleVariance(newSettings.sensorAngVar)
            slam.changeDistanceVariance(newSettings.sensorDistVar)
            slam.changeNumParticles(newSettings.numParticles)
        }
        return newSettings
    }

    fun iterate(curResults: SubconsciousActedResults): RobotCoreActedResults {
        val measurements = curResults.measurements
        val odoPose = curResults.pilotPoses.odoPose

        val detectedFeatures = featureDetector.getFeatures(measurements)

        slam.addTimeStep(detectedFeatures, odoPose)
        val avgPoseAfter = slam.avgPose

        map.incorporateMeasurements(measurements, avgPoseAfter)

        accurateOdo.setInputPoses(avgPoseAfter, odoPose)

        features = detectedFeatures
        subconcResults = curResults
        particlePoses = slam.particlePoses

        if (currentPlanItrs > 0 && currentPlanItrs % REQ_MAN_INTERVAL == 0) {
            reqPlannerManeuvers()
        }

        if (currentPlanItrs == UPDATE_PLAN_START_POSE_INTERVAL) {
            planFrom(avgPoseAfter, map.obstacleTree)
            currentPlanItrs = 0
        }

        if ((numItrs % UPDATE_PLAN_END_POSE_INTERVAL) == 0L) {
            val randomEnd = RobotPose(0, 0f, (random.nextFloat() - 0.5f) * MAX_PLAN_DIST,
                (random.nextFloat() - 0.5f) * MAX_PLAN_DIST, 0f)
            planTo(randomEnd)
        }

        val results = RobotCoreActedResults(System.currentTimeMillis(), features, map.obstacleList, avgPoseAfter,
            maneuvers, paths, curResults, particlePoses, numItrs)

        currentPlanItrs++
        numItrs++

        println("finished iteration $numItrs")
        robotStorage.saveTimeStep(results)
        return results
    }
}