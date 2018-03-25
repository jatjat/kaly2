package ca.joelathiessen.kaly2.tests.pc.unit.slam

import Jama.Matrix
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import ca.joelathiessen.kaly2.slam.landmarks.LandmarksTree
import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.ArrayList

@RunWith(MockitoJUnitRunner::class)
class NNDataAssociatorTest() {

    val DELTA = 0.0001f

    private fun makeFeatFromXY(pose: Pose, x: Float, y: Float): Feature {
        val sensorX = pose.x
        val sensorY = pose.y
        val stDev = 0.0f

        val deltaX = x - sensorX
        val deltaY = y - sensorY
        val dist = FloatMath.sqrt((deltaX * deltaX + deltaY * deltaY))
        val angle = FloatMath.atan2(deltaY, deltaX)
        return Feature(sensorX, sensorY, dist, angle, deltaX, deltaY, stDev)
    }

    @Test
    fun simpleOffsetAssociation() {
        val assoc = NNDataAssociator(1000.0f)
        val pose = Pose(1f, 1f, 1f)
        val features = ArrayList<Feature>()
        var landmarks = LandmarksTree()
        for (index in 0..10) {
            val indexFloat = index.toFloat()
            features.add(makeFeatFromXY(pose, indexFloat, indexFloat))
            landmarks.markForInsertOnCopy(Landmark(indexFloat, indexFloat, Matrix(0, 0)))
        }
        val landmarksMade = landmarks.copy()
        val featuresToLandmarks = assoc.associate(pose, features, landmarksMade)

        for ((feat, land) in featuresToLandmarks) {
            if (feat.x != 10.0f) {
                assertEquals(feat.x, land!!.x, DELTA)
                assertEquals(feat.y, land.y, DELTA)
            } else { // the furthest feature should match the furthest landmark
                assertEquals(feat.x, land!!.x, DELTA)
                assertEquals(feat.y, land.y, DELTA)
            }
        }
    }
}