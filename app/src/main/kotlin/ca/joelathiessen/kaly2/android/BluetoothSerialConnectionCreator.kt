package ca.joelathiessen.kaly2.android

import ca.joelathiessen.kaly2.core.ev3.SerialConnection
import ca.joelathiessen.kaly2.core.ev3.SerialConnectionCreator

class BluetoothSerialConnectionCreator: SerialConnectionCreator {
    override fun createConnection(): SerialConnection? {
        return null // TODO: Setup Bluetooth connection
    }
}