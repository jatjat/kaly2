package ca.joelathiessen.kaly2.android

import android.bluetooth.BluetoothAdapter
import ca.joelathiessen.kaly2.core.ev3.SerialConnection
import ca.joelathiessen.kaly2.core.ev3.SerialConnectionCreator
import java.util.*

class BluetoothSerialConnectionCreator: SerialConnectionCreator {
    var uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    var connectionName: String = ""

    override fun createConnection(): SerialConnection? {
        var conn: BluetoothSerialConnection? = null

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && adapter.isEnabled) {
            val device = adapter.bondedDevices.find { it.name == connectionName }
            val sock = device?.createRfcommSocketToServiceRecord(uuid)

            if (sock != null) {
                try {
                    sock.connect()
                    conn = BluetoothSerialConnection(sock)
                    println("Connected to $connectionName")
                } catch (except: Exception) {
                    println("Failed to connect to $connectionName")
                    sock.close()
                }
                }
        }
        return conn
    }
}
