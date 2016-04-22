package ca.joelathiessen.kaly2

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class Commander(private val robot: Robot) {

    private val br: BufferedReader
    private val robotThread: Thread


    init {
        robotThread = Thread(robot)
        br = BufferedReader(InputStreamReader(System.`in`))

    }

    fun takeCommands() {
        var input: String?
        var quit = false
        println("Enter a command (q to quit, h for help):")
        robotThread.start()
        try {

            while (quit == false) {
                input = br.readLine()
                if (input == null || input == "q") {

                    quit = true
                } else if (input == "g") {
                    robot.startRobot()
                } else if (input == "r") {
                    robot.stopRobot()
                } else if (input == "h") {
                    println("Press the 'g' key to start the robot, 'r' to stop it, and 'q' to quit.")
                }
            }
            robot.stopRobot()

        } catch (io: IOException) {
            io.printStackTrace()
        }

        robotThread.interrupt()

        try {
            robotThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            System.exit(1)
        }

        println("Program successfully completed")
    }

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            // the robot will start itself from here; i.e. :
            // Commander commander = new Commander(robot);
            // ...

        }
    }

}
