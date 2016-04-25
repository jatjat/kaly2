package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.RobotInfo
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import ca.joelathiessen.kaly2.subconscious.sensor.SensorInfo
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.util.*

class FastSlam(val startPose: Pose = Pose(), val dataAssoc: DataAssociator, val partResamp: ParticleResampler, val sensorInfo: SensorInfo, val robotInfo: RobotInfo) : Slam {

    val NUM_PARTICLES = 200

    lateinit var particles: ArrayList<Particle>


    init {
        for (i in 0..NUM_PARTICLES) {
            particles.add(Particle(startPose))
        }
    }

    override fun resetTimeSteps() {
        throw UnsupportedOperationException()
    }

    override fun addTimeStep(features: List<Feature>, pose: Pose, time: Long) {

        //TODO: use info interfaces
        val dist = 0f
        val distVar = 0f
        val rot = 0f
        val rotVar = 0f
        for (particle in particles) {
            particle.moveRandom(dist, distVar, rot, rotVar)
        }

        for (particle in particles) {
            val featuresToLandmarks: Map<Feature, Pair<Point, Landmark>> = dataAssoc.associate(particle.pose, features, particle.landmarks)
        }
    }

    override fun getCurPos(): Pose {
        throw UnsupportedOperationException()
    }
}
