package ca.joelathiessen.kaly2.core.server.messages

import com.google.gson.annotations.Expose

class RobotSessionSettingsResponse(
    val sessionID: Long?,
    val isRunning: Boolean
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "robotSessionSettings"
    }
}