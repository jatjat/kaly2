package ca.joelathiessen.kaly2.server.messages

import com.google.gson.annotations.Expose

class RTPastSlamInfosReqMsg(
    val sessionID: Long,
    val firstItr: Long,
    val lastItr: Long
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override val MSG_TYPE = MSG_TYPE_NAME

    companion object {
        val MSG_TYPE_NAME = "pastSlamInfos"
    }
}
