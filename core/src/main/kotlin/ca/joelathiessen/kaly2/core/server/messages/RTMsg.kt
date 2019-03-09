package ca.joelathiessen.kaly2.core.server.messages

/**
 * Since we're always using one HTTP URI when we communicate in realtime,
 * use "msgType" to record the message destination.
 * This allows both HTTP REST messages and websocket somewhat-REST
 * messages to be treated identically
 *
 **/
class RTMsg(val msg: RTRobotMsg, val requestingNoNetworkSend: Boolean = false) {
    val msgType = msg.MSG_TYPE
}