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
    private val ADD_REMOVE_SEED = 1L
    val DEFAULT_NUM_PARTICLES = 20
    val DEFAULT_DIST_VARIANCE = 1.0
    val DEFAULT_ANG_VARIANCE = 0.01
    val IDENTITY_VARIANCE = 0.2

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
            particles.add(Particle(startPose, 1.0 / DEFAULT_NUM_PARTICLES))
        }
    }

    fun changeNumParticles(number: Int = DEFAULT_NUM_PARTICLES) {
        var remCnt = 0
        for (i in number..particles.size.toInt() - 1) {
            particles.removeAt(Random(ADD_REMOVE_SEED).nextInt(particles.size))
            remCnt++
        }
        var addCnt = 0
        val newParticles = ArrayList<Particle>()
        for (i in particles.size.toInt()..number - 1) {
            newParticles += particles[Random(ADD_REMOVE_SEED).nextInt(particles.size)].copy()
            addCnt++
        }
        particles.addAll(newParticles)

        particles.forEach { it.weight = 1.0 / numParticles }
        println("Removed: ${remCnt}, added: ${addCnt}, total num particles: ${numParticles}")
    }

    fun changeDistanceVariance(variance: Double = DEFAULT_DIST_VARIANCE) {
        distVariance = variance
        R = createR(distVariance, angleVariance, IDENTITY_VARIANCE)
    }

    fun changeAngleVariance(variance: Double = DEFAULT_ANG_VARIANCE) {
        angleVariance = variance
        R = createR(distVariance, angleVariance, IDENTITY_VARIANCE)
    }

    private fun createR(distVar: Double, angVar: Double, identVar: Double): Matrix {
        return Matrix(arrayOf(
                doubleArrayOf(distVar, 0.0, 0.0),
                doubleArrayOf(0.0, angVar, 0.0),
                doubleArrayOf(0.0, 0.0, identVar)
        ))
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
            val newParticle = Particle(Pose(particle.pose.x, particle.pose.y, particle.pose.heading), particle.weight, particle.landmarks)
            val featuresToLandmarks: Map<Feature, Landmark?> = dataAssoc.associate(particle.pose, features, particle.landmarks)

            for ((feat, land) in featuresToLandmarks) {
                if (land != null) {

                    //Using the distance and angle from the particle to the landmark...
                    val dX = land.x - particle.pose.x
                    val dY = land.y - particle.pose.y
                    val particleDist = Math.sqrt((dX * dX) + (dY * dY))
                    val particleAngle = Math.atan2(dY, dX)

                    //...and the distance and angle from the sensor to the feature, find the residual:
                    val residual = Matrix(arrayOf(
                            doubleArrayOf(feat.distance - particleDist),
                            doubleArrayOf(feat.angle - particleAngle),
                            doubleArrayOf(0.0)
                    ))

                    val G = feat.makeJacobian()
                    val GPrime = G.transpose()
                    val E = land.covariance

                    //Find the Kalman gain:
                    val Q = GPrime.times(E).times(G).plus(R)
                    val K = E.times(G).times(Q.inverse())

                    //Mix the ideal and real sensor measurements to update landmark's position:
                    val dPos = K.times(residual)
                    val updatedX = land.x + dPos[0, 0]
                    val updatedY = land.y + dPos[1, 0]

                    //Update the landmark's covariance:
                    val I = Matrix.identity(K.rowDimension, K.columnDimension)
                    val updatedCovar = I.minus(K.times(GPrime)).times(E)

                    val updatedLand = Landmark(updatedX, updatedY, updatedCovar)

                    //Update particle's weight:
                    val firstPart = Math.pow(Q.times(2 * Math.PI).norm2(), -0.5)
                    val secondPart = Math.exp((residual.transpose().times(-0.5)
                            .times(Q.inverse()).times(residual))[0, 0])
                    newParticle.weight = newParticle.weight * firstPart * secondPart

                    newParticle.landmarks.markForUpdateOnCopy(updatedLand, land)
                } else {
                    //we have a new landmark!
                    val G = feat.makeJacobian()
                    val GInverse = G.transpose()
                    val variance = GInverse.times(R).times(GInverse.transpose())
                    newParticle.landmarks.markForInsertOnCopy(Landmark(particle.pose, feat, variance))
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