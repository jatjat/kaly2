package ca.joelathiessen.kaly2.core.server.messages

import com.google.gson.annotations.Expose

class RobotSessionSettingsResponse(
    val sessionID: Long?,
    val isRunning: Boolean,
    // The February 2018 page only draws the robot when this is false. Remove once the
    // 2018-19 page is on the live site
    val hasJustReset: Boolean = false
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "robotSessionSettings"
    }
}