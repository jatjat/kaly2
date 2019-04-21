package ca.joelathiessen.kaly2.ev3

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonParser
import lejos.hardware.Bluetooth
import lejos.hardware.Button
import lejos.hardware.motor.NXTRegulatedMotor
import lejos.hardware.port.MotorPort
import lejos.remote.nxt.NXTConnection
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class MainLoop {
    private val AFTER_WRITE_SLEEP_MS = 50L
    private val NO_READ_INPUT_SLEEP_MS = 50L
    private val MAX_CLOCKWISE = 180
    private val MAX_COUNTER_CLOCKWISE = 180
    private val DEGS_TO_RADS = Math.PI.toFloat() / 180f
    private val QUIT_THREAD_JOIN_TIMEOUT = 1000L

    private val SENSING_SPEED = 10
    private val STEERING_SPEED = 10
    private val DRIVING_SPEED = 10

    private val gson = Gson()

    private val sensing = NXTRegulatedMotor(MotorPort.A)
    private val sensingLock = Any()

    private val steering = NXTRegulatedMotor(MotorPort.A)
    private val steeringLock = Any()

    private val driving = NXTRegulatedMotor(MotorPort.A)
    private val drivingLock = Any()

    private val readerLock = Any()
    private var reader: BufferedReader? = null

    private var spinningClockwise = false

    private val quit = AtomicBoolean(false)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MainLoop().run()
        }
    }

    fun run() {
        sensing.speed = SENSING_SPEED
        driving.speed = DRIVING_SPEED
        steering.speed = STEERING_SPEED

        val outputThread = thread(name = "Output") {
            while (quit.get() == false) {
                println("Waiting for connection")
                val newConn = Bluetooth.getNXTCommConnector().waitForConnection(Int.MAX_VALUE, NXTConnection.RAW) //connectionProvider.getConnection()
                if (newConn != null) {
                    println("Connection obtained")

                    val dataOut = newConn.openDataOutputStream()
                    val writer = BufferedWriter(OutputStreamWriter(dataOut))
                    val dataIn = newConn.openDataInputStream()
                    val newReader = BufferedReader(InputStreamReader(dataIn))
                    synchronized(readerLock) {
                        reader = newReader
                    }
                    var haveConnection = true
                    while (haveConnection == true && quit.get() == false) {
                        val sensorAngle = degsInRads(synchronized(sensingLock) { sensing.tachoCount }.toFloat())
                        val sensorSpinning = synchronized(sensingLock) { sensing.isMoving }
                        val steerAngle = degsInRads(synchronized(steeringLock) { steering.tachoCount }.toFloat())
                        val travelAngle = degsInRads(synchronized(drivingLock) { driving.tachoCount }.toFloat())
                        val travelSpeed = degsInRads(synchronized(drivingLock) { driving.speed }.toFloat())

                        try {
                            val lineOut = gson.toJson(RobotInfo(steerAngle, sensorAngle, sensorSpinning, travelAngle, travelSpeed)) + "\n"
                            writer.write(lineOut)
                            writer.flush()
                            println("sent output: $lineOut")
                        } catch (except: Exception) {
                            makeSafe()
                            println("Error sending output: ${except.stackTrace}")
                            haveConnection = false
                            writer.close()
                        }
                        Thread.sleep(AFTER_WRITE_SLEEP_MS)
                    }
                    newConn.close()
                }
            }
        }

        val inputThread = thread(name = "Input") {
            while (quit.get() == false) {
                try {
                    val localReader = synchronized(readerLock) {
                        reader
                    }
                    print("Set local reader")
                    if (localReader != null) {
                        val lineIn = localReader.readLine()
                        print("Got message: $lineIn")

                        val dataJson = JsonParser().parse(lineIn).obj
                        val msgType = dataJson[MSG_TYPE].string
                        val command = when (msgType) {
                            ROBOT_PILOT -> {
                                gson.fromJson<RobotPilotCommand>(lineIn)
                            }
                            ROBOT_SPINNER -> {
                                gson.fromJson<RobotSpinnerCommand>(lineIn)
                            }
                            else -> {
                                throw IllegalArgumentException("Unknown message type: $msgType")
                            }
                        }

                        when (command) {
                            is RobotSpinnerCommand -> {
                                println("got robot spinner command")
                                spinningClockwise = !spinningClockwise
                                val ang = if (spinningClockwise) MAX_COUNTER_CLOCKWISE else MAX_CLOCKWISE
                                synchronized(sensingLock) {
                                    sensing.rotateTo(ang, true)
                                    print("sensing is at ${sensing.tachoCount}")
                                    print("rotating sensing to $ang")
                                }
                            }
                            is RobotPilotCommand -> {
                                val steerAngle = radsInDegs(command.steerAngle).toInt()
                                val drivingSpeed = radsInDegs(command.travelSpeed).toInt()

                                synchronized(steeringLock) {
                                    steering.rotateTo(steerAngle, true)
                                }
                                synchronized(drivingLock) {
                                    driving.speed = drivingSpeed
                                    driving.rotate(Int.MAX_VALUE, true)
                                }
                            }
                        }
                        localReader.close()
                    } else {
                        Thread.sleep(NO_READ_INPUT_SLEEP_MS)
                    }
                } catch (except: Exception) {
                    println("Error reading input: ${except.stackTrace}")
                    makeSafe()
                }
            }
        }

        Button.ENTER.waitForPressAndRelease()
        quit.set(true)
        outputThread.join(QUIT_THREAD_JOIN_TIMEOUT)
        inputThread.join(QUIT_THREAD_JOIN_TIMEOUT)
        sensing.close()
        steering.close()
        driving.close()
    }

    private fun makeSafe() {
        synchronized(drivingLock) {
            driving.speed = 0
        }
    }

    private fun degsInRads(degrees: Float): Float {
        return degrees * DEGS_TO_RADS
    }
    private fun radsInDegs(rads: Float): Float {
        return rads / DEGS_TO_RADS
    }
}
