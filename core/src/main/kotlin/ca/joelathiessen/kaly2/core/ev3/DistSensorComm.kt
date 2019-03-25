package ca.joelathiessen.kaly2.core.ev3

class DistSensorComm(val messenger: TextMessenger) {

    fun subscribeToDistSensorInfo(onDist: (dist: Float) -> Unit) {
        messenger.setTextSubscriber { text ->
            val dist = text.toFloat()
            onDist(dist)
        }
    }

    fun unsubscribeFromDistSensorInfo() {
        messenger.unsubscribeFromText()
    }

    fun stopCommunication() {
        messenger.stopCommunication()
    }
}