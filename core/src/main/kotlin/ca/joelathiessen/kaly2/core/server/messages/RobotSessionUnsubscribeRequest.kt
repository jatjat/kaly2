package ca.joelathiessen.kaly2.core.server.messages

import com.google.gson.annotations.Expose

class RobotSessionUnsubscribeRequest(
    val sessionID: Long
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "robotSessionUnsubscribe"
    }
}
