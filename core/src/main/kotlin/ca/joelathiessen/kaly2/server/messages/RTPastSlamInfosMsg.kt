package ca.joelathiessen.kaly2.server.messages

import com.google.gson.annotations.Expose

class RTPastSlamInfosMsg(
    val slamInfos: List<RTSlamInfoMsg>
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override var MSG_TYPE = "pastSlamInfos"
}