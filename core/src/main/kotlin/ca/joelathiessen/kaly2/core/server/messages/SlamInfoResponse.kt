package ca.joelathiessen.kaly2.core.server.messages

import ca.joelathiessen.kaly2.core.featuredetector.Feature
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.slam.Particle
import ca.joelathiessen.kaly2.core.slam.landmarks.Landmark
import com.google.gson.annotations.Expose

class RTSlamInfoMsg(
    val sessionID: Long,
    val iteration: Long,
    val timestamp: Long,
    val particles: List<RTParticle>,
    val features: List<RTFeature>,

    val bestPose: RTPose,
    val odoPose: RTPose,
    val truePose: RTPose?
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override var MSG_TYPE = "slamInfo"
}

class RTPose(
    val x: Float,
    val y: Float,
    val theta: Float
) {
    constructor(pose: RobotPose): this(pose.x, pose.y, pose.heading)
}

class RTFeature(
    val distance: Float,
    val angle: Float,
    val stdDev: Float
) {
    constructor(feature: Feature): this(feature.distance, feature.angle, feature.stdDev)
}

class RTLandmark(
    val x: Float,
    val y: Float,
    val stdDev: Float
) {
    constructor(landmark: Landmark): this(landmark.x, landmark.y, Math.sqrt(landmark.covariance.get(0, 0)).toFloat())
}

class RTParticle(
    val x: Float,
    val y: Float,
    val theta: Float,
    val Landmarks: List<RTLandmark>
) {
    constructor(particle: Particle): this(particle.pose.x, particle.pose.y, particle.pose.heading, particle.landmarks.getList().map { RTLandmark(it) })
}