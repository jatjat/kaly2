package ca.joelathiessen.kaly2.server.messages

import com.google.gson.annotations.Expose

class RobotSettingsMsg(
    val running: Boolean,
    val resetting: Boolean
) : RobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "robotSettings"
    }
}

class FastSlamSettingsMsg(
    val numParticles: Int,
    val sensorDistVar: Float,
    val sensorAngVar: Float
) : RobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "fastSlamSettings"
    }
}
