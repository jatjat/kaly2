package ca.joelathiessen.kaly2.core.ev3

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson

class RobotComm(private val messenger: TextMessenger) {
    private val gson = Gson()
    private val subscribers = HashSet<(info: RobotInfo) -> Unit>()

    fun subscribeToRobotInfo(onRobotInfo: (info: RobotInfo) -> Unit) {
        messenger.setTextSubscriber { text ->
            val info = gson.fromJson<RobotInfo>(text)
            synchronized(subscribers) {
                subscribers.forEach {
                    it(info)
                }
            }
        }
    }

    fun unsubscribeFromRobotInfo(onRobotInfo: (info: RobotInfo) -> Unit) {
        synchronized(subscribers) {
            subscribers.remove(onRobotInfo)
        }
    }

    fun driveAtAngle(steerAngle: Float, driveSpeed: Float) {
        val pilotCommand = gson.toJson(RobotPilotCommand(steerAngle, driveSpeed))
        messenger.sendText(pilotCommand)
    }

    fun spinSensor() {
        val spinnerCommand = gson.toJson(RobotSpinnerCommand(true))
        messenger.sendText(spinnerCommand)
    }

    fun stopCommunication() {
        messenger.stopCommunication()
    }
}