package ca.joelathiessen.kaly2.server.messages

import com.google.gson.annotations.Expose

class RTRobotSessionSubscribeRespMsg(
    val sessionID: Long?,
    val success: Boolean,
    val alternateServerAddress: String?
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "robotSessionSubscribe"
    }
}
