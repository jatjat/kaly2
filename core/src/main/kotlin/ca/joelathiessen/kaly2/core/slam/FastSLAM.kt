package ca.joelathiessen.kaly2.core.slam

import Jama.Matrix
import ca.joelathiessen.kaly2.core.featuredetector.Feature
import ca.joelathiessen.kaly2.core.odometry.CarModel
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.slam.landmarks.Landmark
import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose
import java.util.ArrayList
import java.util.Random

class FastSLAM(
    val startPose: RobotPose,
    private val carMotionModel: CarModel,
    private val dataAssoc: DataAssociator,
    private val partResamp: ParticleResampler
) : Slam {
    private val ADD_REMOVE_SEED = 1L
    val DEFAULT_NUM_PARTICLES = 20
    val DEFAULT_DIST_VARIANCE = 1.0f
    val DEFAULT_ANG_VARIANCE = 0.01f
    val IDENTITY_VARIANCE = 0.2f

    var numParticles = DEFAULT_NUM_PARTICLES
        get() = particles.size
        private set

    var distVariance = DEFAULT_DIST_VARIANCE
        private set

    var angleVariance = DEFAULT_ANG_VARIANCE
        private set

    private var R = createR(distVariance, angleVariance, IDENTITY_VARIANCE)

    private var lastKnownPose = startPose

    private var particles = ArrayList<Particle>(DEFAULT_NUM_PARTICLES)

    init {
        for (i in 1..DEFAULT_NUM_PARTICLES) {
            particles.add(Particle(startPose, 1.0f / DEFAULT_NUM_PARTICLES))
        }
    }

    fun changeNumParticles(number: Int = DEFAULT_NUM_PARTICLES) {
        var remCnt = 0
        for (i in number..particles.size - 1) {
            particles.removeAt(Random(ADD_REMOVE_SEED).nextInt(particles.size))
            remCnt++
        }
        var addCnt = 0
        val newParticles = ArrayList<Particle>()
        for (i in particles.size..number - 1) {
            newParticles += particles[Random(ADD_REMOVE_SEED).nextInt(particles.size)].copy()
            addCnt++
        }
        particles.addAll(newParticles)

        particles.forEach { it.weight = 1.0f / numParticles }
        println("Removed: $remCnt, added: $addCnt, total num particles: $numParticles")
    }

    fun changeDistanceVariance(variance: Float = DEFAULT_DIST_VARIANCE) {
        distVariance = variance
        R = createR(distVariance, angleVariance, IDENTITY_VARIANCE)
    }

    fun changeAngleVariance(variance: Float = DEFAULT_ANG_VARIANCE) {
        angleVariance = variance
        R = createR(distVariance, angleVariance, IDENTITY_VARIANCE)
    }

    private fun createR(distVar: Float, angVar: Float, identVar: Float): Matrix {
        return Matrix(arrayOf(
            doubleArrayOf(distVar.toDouble(), 0.0, 0.0),
            doubleArrayOf(0.0, angVar.toDouble(), 0.0),
            doubleArrayOf(0.0, 0.0, identVar.toDouble())
        ))
    }

    override fun resetTimeSteps() {
        throw UnsupportedOperationException()
    }

    override fun addTimeStep(features: List<Feature>, robotPose: RobotPose) {
        val curPose = robotPose

        particles.forEach { it.moveRandom(lastKnownPose, curPose, carMotionModel) }

        val newParticles = ArrayList<Particle>(particles.size)

        for (particle in particles) {
            val newParticle = Particle(Pose(particle.pose.x, particle.pose.y, particle.pose.heading), particle.weight, particle.landmarks)
            val featuresToLandmarks: Map<Feature, Landmark?> = dataAssoc.associate(particle.pose, features, particle.landmarks)

            for ((feat, land) in featuresToLandmarks) {
                val G = feat.makeJacobian()
                val GPrime = G.transpose()

                if (land != null) {

                    //Using the distance and angle from the particle to the landmark...
                    val deltaX = land.x - particle.pose.x
                    val deltaY = land.y - particle.pose.y
                    val particleDist = FloatMath.sqrt((deltaX * deltaX) + (deltaY * deltaY))
                    val particleAngle = FloatMath.atan2(deltaY, deltaX)

                    //...and the distance and angle from the sensor to the feature, find the residual:
                    val residual = Matrix(arrayOf(
                        doubleArrayOf(feat.distance - particleDist.toDouble()),
                        doubleArrayOf(feat.angle - particleAngle.toDouble()),
                        doubleArrayOf(0.0)
                    ))

                    //Find the Kalman gain:
                    val E = land.covariance
                    val Q = GPrime.times(E).times(G).plus(R)
                    val K = E.times(G).times(Q.inverse())

                    //Mix the ideal and real sensor measurements to update landmark's position:
                    val dPos = K.times(residual)
                    val updatedX = land.x + dPos[0, 0].toFloat()
                    val updatedY = land.y + dPos[1, 0].toFloat()

                    //Update the landmark's covariance:
                    val I = Matrix.identity(K.rowDimension, K.columnDimension)
                    val updatedCovar = I.minus(K.times(GPrime)).times(E)

                    val updatedLand = Landmark(updatedX, updatedY, updatedCovar)

                    //Update particle's weight:
                    val firstPart = FloatMath.pow(Q.times(2 * Math.PI).norm2().toFloat(), -0.5f)
                    val secondPart = FloatMath.exp((residual.transpose().times(-0.5)
                        .times(Q.inverse()).times(residual))[0, 0].toFloat())
                    newParticle.weight = newParticle.weight * firstPart * secondPart

                    newParticle.landmarks.markForUpdateOnCopy(updatedLand, land)
                } else {
                    //we have a new landmark!
                    val GInv = G.inverse()
                    val variance = GInv.times(R).times(GInv.transpose())
                    newParticle.landmarks.markForInsertOnCopy(Landmark(particle.pose, feat, variance))
                }
            }
            newParticles += newParticle
        }

        lastKnownPose = curPose
        particles = partResamp.resample(newParticles)
    }

    override var particlePoses: List<Pose> = ArrayList()
        get() {
            return particles.map { Pose(it.pose.x, it.pose.y, it.pose.heading) }
        }
        private set

    override var avgPose: RobotPose = RobotPose(0, 0f, 0f, 0f, 0f)
        get() {
            val xAvg = particles.map { it.pose.x }.sum() / particles.size
            val yAvg = particles.map { it.pose.y }.sum() / particles.size
            val thetaAvg = particles.map { it.pose.heading }.sum() / particles.size
            return RobotPose(lastKnownPose.time, 0f, xAvg, yAvg, thetaAvg)
        }
        private set
}