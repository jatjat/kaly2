package ca.joelathiessen.kaly2.tests.pc.unit.slam

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

    @Test
    fun simpleOffsetAssociation() {
        val assoc = NNDataAssociator()
        val pose = Pose(1f,1f,1f)
        val features = ArrayList<Feature>()
        val landmarks = LandmarksTree()
        for(index in 0..10) {
            val indexFloat = index.toFloat()
            features.add(Feature(indexFloat, indexFloat))
            landmarks.addLandmark(Landmark(indexFloat, indexFloat))
        }

        val featuresToLandmarks = assoc.associate(pose, features, landmarks)

        for( featLand in featuresToLandmarks) {
            if(featLand.key.x != 10f) {
                assertEquals(featLand.key.x + 1.0f, featLand.value.x)
                assertEquals(featLand.key.y + 1.0f, featLand.value.y)
            } else { // the furthest feature should match the furthest landmark
                assertEquals(featLand.key.x, featLand.value.x)
                assertEquals(featLand.key.y, featLand.value.y)
            }
        }
    }
}