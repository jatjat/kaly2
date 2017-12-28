package ca.joelathiessen.kaly2.server.messages

import com.google.gson.annotations.Expose

class SlamInfoMsg(
    val sessionID: Long,
    val timestamp: Long,
    val particles: List<RTParticle>,
    val features: List<RTFeature>,

    val bestPose: RTPose,
    val odoPose: RTPose,
    val truePose: RTPose,
    val trueLandmarks: List<RTLandmark>
) : RobotMsg {
    @Expose(serialize = false, deserialize = false)
    override var MSG_TYPE = "slamInfo"
}

class RTPose(
    val x: Float,
    val y: Float,
    val theta: Float
)

class RTFeature(
    val distance: Float,
    val angle: Float,
    val stdDev: Float
)

class RTLandmark(
    val x: Float,
    val y: Float,
    val stdDev: Float
)

class RTParticle(
    val x: Float,
    val y: Float,
    val theta: Float,
    val Landmarks: List<RTLandmark>
)