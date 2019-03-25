package ca.joelathiessen.kaly2.core.ev3

import java.io.BufferedWriter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class TextMessenger(private val connectionCreator: SerialConnectionCreator) {
    val CONN_FAILED_WAIT_MS = 100L
    private var cont = AtomicBoolean(true)
    private val subscriberLock = Any()
    private var subscriber: ((text: String) -> Unit)? = null
    private val writerLock = Any()
    private var writer: BufferedWriter? = null

    init {
        thread {
            while (cont.get() == true) {
                synchronized(writerLock) {
                    writer = null
                }
                val conn = connectionCreator.createConnection()
                if (conn == null) {
                    Thread.sleep(CONN_FAILED_WAIT_MS)
                } else {
                    val reader = conn.getInputReader()
                    synchronized(writerLock) {
                        writer = conn.getOutputWriter()
                    }
                    var couldRead = true
                    try {
                        while (couldRead == true && cont.get() == true) {
                            val line = reader.readLine()
                            if (line == null) {
                                couldRead = false
                            } else {
                                synchronized(subscriberLock) {
                                    subscriber?.invoke(line)
                                }
                            }
                        }
                    } finally {
                        conn.close()
                    }
                }
            }
        }
    }

    fun setTextSubscriber(onText: (text: String) -> Unit) {
        subscriber = onText
    }

    fun unsubscribeFromText() {
        subscriber = null
    }

    fun sendText(text: String) {
        synchronized(writerLock) {
            writer?.write(text)
        }
    }

    fun stopCommunication() {
        cont.set(false)
        synchronized(subscriberLock) {
            subscriber = null
        }
    }
}