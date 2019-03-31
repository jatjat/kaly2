package ca.joelathiessen.kaly2.android

import android.bluetooth.BluetoothSocket
import ca.joelathiessen.kaly2.core.ev3.SerialConnection
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BluetoothSerialConnection(private val socket: BluetoothSocket): SerialConnection {
    override val inputReader = BufferedReader(InputStreamReader(socket.inputStream))
    override val outputWriter = BufferedWriter(OutputStreamWriter(socket.outputStream))

    override fun close() {
        socket.close()
    }
}