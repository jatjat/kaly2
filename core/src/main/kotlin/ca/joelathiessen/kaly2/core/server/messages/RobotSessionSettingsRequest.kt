package ca.joelathiessen.kaly2.core.server.messages

import com.google.gson.annotations.Expose

class RobotSessionSettingsRequest(
    val sessionID: Long,
    val shouldRun: Boolean
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "robotSessionSettings"
    }
}
