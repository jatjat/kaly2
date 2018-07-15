package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.map.GlobalMap
import ca.joelathiessen.kaly2.map.MapTree
import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.persistence.PersistentStorage
import ca.joelathiessen.kaly2.planner.GlobalPathPlanner
import ca.joelathiessen.kaly2.planner.linear.LinearPathSegmentRootFactory
import ca.joelathiessen.kaly2.slam.FastSLAM
import ca.joelathiessen.kaly2.slam.FastUnbiasedResampler
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.subconscious.LocalPlanner
import ca.joelathiessen.kaly2.subconscious.SimulatedPilot
import ca.joelathiessen.kaly2.subconscious.sensor.SimSensor
import ca.joelathiessen.kaly2.subconscious.sensor.SimSpinner
import ca.joelathiessen.util.array2d
import ca.joelathiessen.util.image.AndroidJVMImage
import ca.joelathiessen.util.image.Color
import lejos.robotics.geometry.Point
import org.joda.time.DateTime
import java.util.Collections

class SimRobotSessionFactory(
    private val odoAngStdDev: Float,
    private val odoDistStdDev: Float,
    private val stepDist: Float,
    private val maxPilotDist: Float,
    private val maxPilotRot: Float,
    private val sensorStartAng: Float,
    private val sensorEndAng: Float,
    private val sensorAngIncr: Float,
    private val mapImage: AndroidJVMImage,
    private val maxSensorRange: Float,
    private val sensorDistStdDev: Float,
    private val sensorAngStdDev: Float,
    private val lineThreshold: Float,
    private val checkWithinAngle: Float,
    private val maxRatio: Float,
    private val localPlannerRotStep: Float,
    private val localPlannerDistStep: Float,
    private val localPlannerGridStep: Float,
    private val localPlannerGridSize: Float,
    private val obstacleSize: Float,
    private val globalPlannerSearchDist: Float,
    private val globalPlannerStepDist: Float,
    private val globalPlannerItrs: Int,
    private val mapRemoveInvalidObsInterval: Int,
    private val minSubcMeasTime: Long,
    private val nnAsocThreshold: Float,
    private val persistentStorage: PersistentStorage
) : RobotSessionFactory {
    private val ROBOT_NAME = "simRobot"
    private val MAP_NAME = "defaultMap"

    override fun makeRobotSession(sid: Long, sessionStoppedWithNoSubscribersHandler: () -> Unit): RobotSession {
        val initalGoalPose = RobotPose(0L, 0f, 0f, 0f, 0f)

        val currentDate = DateTime()

        val robotStorage = persistentStorage.getOrMakeRobotStorage(sid, ROBOT_NAME, false, MAP_NAME, currentDate)

        val mapData = extractDataFromMapImage(mapImage)

        val obstacles = MapTree()
        mapData.obstaclePoints.forEach { obstacles.add(it) }

        val simPilot = SimulatedPilot(odoAngStdDev, odoDistStdDev, stepDist, mapData.startPose, maxPilotRot, maxPilotDist)
        val simSpinner = SimSpinner(sensorStartAng, sensorEndAng, sensorAngIncr)
        val simSensor = SimSensor(mapData.obstacleGrid, mapImage.width, mapImage.height,
                maxSensorRange, sensorDistStdDev, sensorAngStdDev, simSpinner, simPilot)

        val rid = robotStorage?.histid // TODO: distinguish meaningfully between rid/histid/sid

        val featureDetector = SplitAndMerge(lineThreshold, checkWithinAngle, maxRatio)

        val motionModel = CarModel()
        val dataAssoc = NNDataAssociator(nnAsocThreshold)
        val partResamp = FastUnbiasedResampler()
        val slam = FastSLAM(mapData.startPose, motionModel, dataAssoc, partResamp)

        val localPlanner = LocalPlanner(0f, localPlannerRotStep, localPlannerDistStep, localPlannerGridStep,
                localPlannerGridSize, obstacleSize)

        val factory = LinearPathSegmentRootFactory()
        val globalPathPlanner = GlobalPathPlanner(factory, obstacles, obstacleSize, globalPlannerSearchDist, globalPlannerStepDist,
                mapData.startPose, initalGoalPose, globalPlannerItrs)

        val map = GlobalMap(obstacleSize, obstacleSize, mapRemoveInvalidObsInterval)

        return RobotSession(rid, sessionStoppedWithNoSubscribersHandler, mapData.startPose, initalGoalPose, simPilot, simSpinner,
                simSensor, featureDetector, minSubcMeasTime, map, robotStorage, slam, localPlanner, globalPathPlanner)
    }

    private data class MapImageData(
        val startPose: RobotPose,
        val goalPose: RobotPose,
        val obstaclePoints: ArrayList<Point>,
        val obstacleGrid: Array<Array<Point?>>
    )
    private fun extractDataFromMapImage(mapImage: AndroidJVMImage): MapImageData {
        var x = mapImage.width / 2f
        var y = mapImage.height / 2f
        var xEnd = mapImage.width / 2f
        var yEnd = mapImage.height / 2f

        val obsGrid = array2d<Point?>(mapImage.width, mapImage.height, { null })
        val points = ArrayList<Point>()
        for (xInc in 0 until mapImage.width) {
            for (yInc in 0 until mapImage.height) {
                if (mapImage.getColor(xInc, yInc) == Color.BLACK) {
                    points.add(Point(xInc.toFloat(), yInc.toFloat()))
                    obsGrid[xInc][yInc] = Point(xInc.toFloat(), yInc.toFloat())
                } else if (mapImage.getColor(xInc, yInc) == Color.RED) {
                    x = xInc.toFloat()
                    y = yInc.toFloat()
                } else if (mapImage.getColor(xInc, yInc) == Color.GREEN) {
                    xEnd = xInc.toFloat()
                    yEnd = yInc.toFloat()
                }
            }
        }
        val startPose = RobotPose(0, 0f, x, y, 0f)
        val goalPose = RobotPose(0, 0f, xEnd, yEnd, 0f)

        Collections.shuffle(points)

        return MapImageData(startPose, goalPose, points, obsGrid)
    }
}