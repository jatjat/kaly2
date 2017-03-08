package ca.joelathiessen.kaly2

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.featuredetector.FeatureDetector
import ca.joelathiessen.kaly2.odometry.AccurateSlamOdometry
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.slam.Slam
import ca.joelathiessen.kaly2.subconscious.SubconsciousThreaded
import ca.joelathiessen.kaly2.subconscious.SubconsciousThreadedResults
import ca.joelathiessen.util.GenTree
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.util.*

class RobotCore(private val initialGoal: RobotPose, private val subConsc: SubconsciousThreaded,
                private val accurateOdo: AccurateSlamOdometry, private val slam: Slam,
                private val featureDetector: FeatureDetector,
                private val globalPathPlannerFactory: (obstacles: GenTree<Point>, curPose: RobotPose,
                                                       goalPose: RobotPose) -> GlobalPathPlanner,
                private val suggestedNumGlobalPathPlannerItrs: Int, private val obstacles: GenTree<Point>) {
    var features: List<Feature> = ArrayList()
        private set

    var maneuvers: List<RobotPose> = ArrayList()
        private set

    var paths: List<PathSegmentInfo> = ArrayList()
        private set

    var subconcResults: SubconsciousThreadedResults? = null
        private set

    var particlePoses: List<Pose> = ArrayList()
        private set

    fun startSubconscious() {
        subConsc.start()
    }

    fun stopSubconscious() {
        subConsc.stop()
    }

    fun iterate() {
        val curResults = subConsc.resultsQueue.take()
        val measurements = curResults.measurements
        if (measurements.size > 0) {
            val odoPose = measurements.first().odoPose as RobotPose

            val detectedFeatures = featureDetector.getFeatures(measurements)

            slam.addTimeStep(features, odoPose)
            val avgPoseAfter = slam.avgPose

            accurateOdo.setInputPoses(avgPoseAfter, odoPose)

            val gblPthPln = globalPathPlannerFactory(obstacles, avgPoseAfter, initialGoal)
            gblPthPln.iterate(suggestedNumGlobalPathPlannerItrs)
            val plannedManeuvers = gblPthPln.getManeuvers()
            subConsc.globalManeuvers.set(plannedManeuvers)

            features = detectedFeatures
            maneuvers = plannedManeuvers
            paths = gblPthPln.paths
            subconcResults = curResults
            particlePoses = slam.particlePoses
        }
    }
}