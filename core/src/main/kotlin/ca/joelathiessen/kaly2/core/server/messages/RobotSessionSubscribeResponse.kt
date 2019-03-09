package ca.joelathiessen.kaly2.core.server.messages

import com.google.gson.annotations.Expose

class RobotSessionSubscribeResponse(
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
