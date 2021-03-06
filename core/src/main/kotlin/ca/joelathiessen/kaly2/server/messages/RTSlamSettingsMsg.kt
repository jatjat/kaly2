package ca.joelathiessen.kaly2.server.messages

import com.google.gson.annotations.Expose

class RTSlamSettingsMsg(
    val sessionID: Long,
    val numParticles: Int,
    val sensorDistVar: Float,
    val sensorAngVar: Float
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "slamSettings"
    }
}
