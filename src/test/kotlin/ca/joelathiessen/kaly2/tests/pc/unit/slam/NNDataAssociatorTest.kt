package ca.joelathiessen.kaly2.tests.pc.unit.slam

import Jama.Matrix
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.NNDataAssociator
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import ca.joelathiessen.kaly2.slam.landmarks.LandmarksTree
import lejos.robotics.navigation.Pose
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*
import org.junit.Assert.*

@RunWith(MockitoJUnitRunner::class)
class NNDataAssociatorTest() {

    val DELTA = 0.0001

    private fun makeFeatFromXY(x: Double, y: Double): Feature {
        val dist = Math.sqrt((x * x + y * y))
        val angle = Math.atan2(y, x)
        val sensorX = 1.0
        val sensorY = 1.0
        val stDev = 0.0
        return Feature(sensorX, sensorY, dist, angle, stDev)
    }

    @Test
    fun simpleOffsetAssociation() {
        val assoc = NNDataAssociator()
        val pose = Pose(1f,1f,1f)
        val features = ArrayList<Feature>()
        var landmarks = LandmarksTree()
        for(index in 0..10) {
            val indexDouble = index.toDouble()
            features.add(makeFeatFromXY(indexDouble, indexDouble))
            landmarks.markForInsertOnCopy(Landmark(indexDouble, indexDouble, Matrix(0,0)))
            landmarks = landmarks.copy()
        }

        val featuresToLandmarks = assoc.associate(pose, features, landmarks)

        for( featLand in featuresToLandmarks) {
            if(featLand.key.x != 10.0) {
                assertEquals(featLand.key.x + 1.0, featLand.value.x, DELTA)
                assertEquals(featLand.key.y + 1.0, featLand.value.y, DELTA)
            } else { // the furthest feature should match the furthest landmark
                assertEquals(featLand.key.x, featLand.value.x, DELTA)
                assertEquals(featLand.key.y, featLand.value.y, DELTA)
            }
        }
    }
}