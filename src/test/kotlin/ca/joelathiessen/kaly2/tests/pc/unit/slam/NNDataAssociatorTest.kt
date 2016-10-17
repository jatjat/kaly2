package ca.joelathiessen.kaly2.tests.pc.unit.slam

import Jama.Matrix
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import ca.joelathiessen.kaly2.slam.landmarks.LandmarksTree
import lejos.robotics.navigation.Pose
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class NNDataAssociatorTest() {

    val DELTA = 0.0001

    private fun makeFeatFromXY(pose: Pose, x: Double, y: Double): Feature {
        val dist = Math.sqrt((x * x + y * y))
        val angle = Math.atan2(y, x)
        val sensorX = 1.0
        val sensorY = 1.0
        val stDev = 0.0
        return Feature(sensorX, sensorY, 0.0, 0.0, stDev)
    }

    @Test
    fun simpleOffsetAssociation() {
        val assoc = NNDataAssociator(1000.0)
        val pose = Pose(1f, 1f, 1f)
        val features = ArrayList<Feature>()
        var landmarks = LandmarksTree()
        for (index in 0..10) {
            val indexDouble = index.toDouble()
            features.add(makeFeatFromXY(pose, indexDouble, indexDouble))
            landmarks.markForInsertOnCopy(Landmark(indexDouble, indexDouble, Matrix(0, 0)))
        }
        val landmarksMade = landmarks.copy()
        val featuresToLandmarks = assoc.associate(pose, features, landmarksMade)

        for ((feat, land) in featuresToLandmarks) {
            if (feat.x != 10.0) {
                assertEquals(feat.x, land!!.x, DELTA)
                assertEquals(feat.y, land.y, DELTA)
            } else { // the furthest feature should match the furthest landmark
                assertEquals(feat.x, land!!.x, DELTA)
                assertEquals(feat.y, land.y, DELTA)
            }
        }
    }
}