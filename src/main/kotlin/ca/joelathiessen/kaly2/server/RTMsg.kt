package ca.joelathiessen.kaly2.server

class RTMsg(
        val timestamp: Long,
        val particles: List<RTParticle>,
        val features: List<RTFeature>,

        val bestPose: RTPose,
        val odoPose: RTPose,
        val truePose: RTPose,
        val trueLandmarks: List<RTLandmark>
)


class RTPose(
        val x: Float,
        val y: Float,
        val theta: Float
)

class RTFeature(
        val distance: Double,
        val angle: Double,
        val stdDev: Double
)

class RTLandmark(
        val x: Double,
        val y: Double,
        val stdDev: Double
)

class RTParticle(
        val x: Float,
        val y: Float,
        val theta: Float,
        val Landmarks: List<RTLandmark>
)