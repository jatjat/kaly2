package ca.joelathiessen.kaly2.slam

import Jama.Matrix
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.odometry.CarModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import ca.joelathiessen.kaly2.subconscious.sensor.SensorInfo
import lejos.robotics.navigation.Pose
import java.util.*

class FastSLAM(val startPose: RobotPose, private val carMotionModel: CarModel, private val dataAssoc: DataAssociator,
               private val partResamp: ParticleResampler, val sensorInfo: SensorInfo) : Slam {
    val NUM_PARTICLES = 100
    val DIST_VARIANCE = 0.1
    val ANGLE_VARIANCE = 0.01
    val IDENTITY_VARIANCE = 0.0001

    private val R = Matrix(arrayOf(
            doubleArrayOf(DIST_VARIANCE, 0.0, 0.0),
            doubleArrayOf(0.0, ANGLE_VARIANCE, 0.0),
            doubleArrayOf(0.0, 0.0, IDENTITY_VARIANCE)
    ))

    private var lastKnownPose = startPose

    private var particles = ArrayList<Particle>(NUM_PARTICLES)

    init {
        for (i in 1..NUM_PARTICLES) {
            particles.add(Particle(startPose, 1.0 / NUM_PARTICLES))
        }
    }

    override fun getCurPos(): RobotPose {
        throw UnsupportedOperationException()
    }

    override fun resetTimeSteps() {
        throw UnsupportedOperationException()
    }

    override fun addTimeStep(features: List<Feature>, curPose: RobotPose) {

        particles.forEach { it.moveRandom(lastKnownPose, curPose, carMotionModel) }

        val newParticles = ArrayList<Particle>(particles.size)

        for (particle in particles) {
            val newParticle = Particle(particle.pose, particle.weight)
            val featuresToLandmarks: Map<Feature, Landmark?> = dataAssoc.associate(particle.pose, features, particle.landmarks)

            for ((feat, land) in featuresToLandmarks) {
                if (land != null) {

                    //Using the distance and angle from the particle to the landmark...
                    val dX = land.x - particle.pose.x
                    val dY = land.y - particle.pose.y
                    val particleDist = Math.sqrt(dX * dX + dY * dY)
                    val particleAngle = Math.atan2(dY, dX)

                    //...and the distance and angle from the sensor to the feature, find the residual:
                    val residual = Matrix(arrayOf(doubleArrayOf(
                            particleDist - feat.distance, particleAngle - (particle.pose.heading + feat.angle))))

                    val G = feat.jacobian
                    val GPrime = G.transpose()
                    val E = land.covariance

                    //Find the Kalman gain:
                    val Q = GPrime.times(E).times(G).plus(R)
                    val K = E.times(G).times(Q.inverse())

                    //Mix the ideal and real sensor measurements to update landmark's position:
                    val dPos = K.times(residual)
                    val updatedX = land.x + dPos[0, 0]
                    val updatedY = land.y + dPos[0, 1]

                    //Update the landmark's covariance:
                    val I = Matrix.identity(K.rowDimension, K.columnDimension)
                    val updatedCovar = I.minus(K.times(GPrime)).times(E)

                    val updatedLand = Landmark(updatedX, updatedY, updatedCovar)

                    //Update particle's weight:
                    val firstPart = Math.pow(Q.times(2 * Math.PI).norm2(), -0.5)
                    val secondPart = Math.exp((residual.transpose().times(-0.5)
                            .times(Q.inverse()).times(residual))[0, 0])
                    newParticle.weight = newParticle.weight * (firstPart * secondPart)

                    newParticle.landmarks.markForUpdateOnCopy(updatedLand, land)
                } else {
                    //we have a new landmark!
                    newParticle.landmarks.markForInsertOnCopy(Landmark(particle.pose, feat, R))
                }
            }
            newParticles += newParticle
        }

        lastKnownPose = curPose
        particles = partResamp.resample(newParticles)
    }

    var particlePoses: List<Pose> = ArrayList()
        get() {
            return particles.map { it.pose }
        }
        private set

    var avgPose: RobotPose = RobotPose(0, 0f, 0f, 0f, 0f)
        get() {
            val xAvg = particles.map { it.pose.x }.sum() / particles.size
            val yAvg = particles.map { it.pose.y }.sum() / particles.size
            val thetaAvg = particles.map { it.pose.heading }.sum() / particles.size
            return RobotPose(lastKnownPose.time, 0f, xAvg, yAvg, thetaAvg)
        }
        private set
}