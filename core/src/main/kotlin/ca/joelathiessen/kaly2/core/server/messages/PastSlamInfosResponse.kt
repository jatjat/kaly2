package ca.joelathiessen.kaly2.core.server.messages

import com.google.gson.annotations.Expose

class PastSlamInfosResponse(
    val slamInfos: List<RTSlamInfoMsg>
) : RTRobotMsg {
    @Expose(serialize = false, deserialize = false)
    override var MSG_TYPE = "pastSlamInfos"
}