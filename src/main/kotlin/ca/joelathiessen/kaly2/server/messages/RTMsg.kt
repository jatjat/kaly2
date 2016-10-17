package ca.joelathiessen.kaly2.server.messages

/**
 * Since we're always using one HTTP URI when we communicate in realtime,
 * use "msgType" to record the message destination.
 * This allows both HTTP REST messages and websocket somewhat-REST
 * messages to be treated identically
 *
 **/
class RTMsg(val msg: RobotMsg) {
    val msgType = msg.MSG_TYPE
}