package ca.joelathiessen.kaly2

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.FeatureDetector
import ca.joelathiessen.kaly2.odometry.AccurateSlamOdometry
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.slam.Slam
import ca.joelathiessen.kaly2.subconscious.SubconsciousActedResults
import ca.joelathiessen.util.GenTree
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.util.*


data class RobotCoreActedResults(val timestamp: Long, val features: List<Feature>,
                                 val maneuvers: List<RobotPose>, val globalPlannerPaths: List<PathSegmentInfo>,
                                 val subconcResults: SubconsciousActedResults,
                                 val particlePoses: List<Pose>, val numItrs: Long)
class RobotCoreActed(private val initialGoal: RobotPose, private val accurateOdo: AccurateSlamOdometry,
                     private val slam: Slam, private val featureDetector: FeatureDetector,
                     private val obstacles: GenTree<Point>) {
    var features: List<Feature> = ArrayList()
        private set

    var maneuvers: List<RobotPose> = ArrayList()

    var paths: List<PathSegmentInfo> = ArrayList()
        private set

    var subconcResults: SubconsciousActedResults? = null
        private set

    var particlePoses: List<Pose> = ArrayList()
        private set

    private var numItrs = 0L

    var reqPlannerManeuvers: () -> Unit = {}
    var sendPlannerManeuversToLocalPlanner: (List<RobotPose>) -> Unit = {}

    fun onManeuverResults(newManeuvers: List<RobotPose>) {
        maneuvers = newManeuvers
        sendPlannerManeuversToLocalPlanner(maneuvers)
    }

    fun onPaths(newPaths: List<PathSegmentInfo>) {
        paths = newPaths
    }

    fun iterate(curResults: SubconsciousActedResults): RobotCoreActedResults {
        val measurements = curResults.measurements
        val odoPose = curResults.pilotPoses.odoPose

        val detectedFeatures = featureDetector.getFeatures(measurements)

        slam.addTimeStep(features, odoPose)
        val avgPoseAfter = slam.avgPose

        accurateOdo.setInputPoses(avgPoseAfter, odoPose)

        features = detectedFeatures
        subconcResults = curResults
        particlePoses = slam.particlePoses

        if(numItrs % 10 == 0L) {
            reqPlannerManeuvers()
        }
        numItrs++
        return RobotCoreActedResults(System.currentTimeMillis(), features, maneuvers, paths, curResults,
                particlePoses, numItrs)
        }
}