package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.CarModel
import ca.joelathiessen.kaly2.RobotPose
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import ca.joelathiessen.kaly2.subconscious.sensor.SensorInfo
import lejos.robotics.navigation.Pose
import java.util.*

class FastSlam(val startPose: Pose = Pose(), private val motionModel: CarModel, private val dataAssoc: DataAssociator, private val partResamp: ParticleResampler, val sensorInfo: SensorInfo) : Slam {

    val NUM_PARTICLES = 200
    lateinit var prevPose: RobotPose

    lateinit var particles: ArrayList<Particle>

    init {
        for (i in 0..NUM_PARTICLES) {
            particles.add(Particle(startPose))
        }
    }

    override fun resetTimeSteps() {
        throw UnsupportedOperationException()
    }

    override fun addTimeStep(features: List<Feature>, robotPose: RobotPose) {

        for (particle in particles) {
            particle.moveRandom(robotPose, prevPose, motionModel)
        }

        for (particle in particles) {
            val featuresToLandmarks: Map<Feature, Landmark> = dataAssoc.associate(particle.pose, features, particle.landmarks)
        }
    }

    override fun getCurPos(): Pose {
        throw UnsupportedOperationException()
    }
}
