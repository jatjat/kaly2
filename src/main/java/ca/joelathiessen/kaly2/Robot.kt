package ca.joelathiessen.kaly2

import ca.joelathiessen.kaly2.subconscious.Measurement
import ca.joelathiessen.kaly2.subconscious.Subconscious
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

class Robot(private val subconsc: Subconscious, private val sweeps: ConcurrentLinkedQueue<ArrayList<Measurement>>) : Runnable {
    private val subconscThread: Thread
    private val runRobot = AtomicBoolean(false)
    private final val lock = ReentrantLock();
    private val runRobot2 = lock.newCondition()

    init {
        subconscThread = Thread(subconsc)
    }

    fun startRobot() {
        if (runRobot.get() == false) {
            runRobot.set(true)

            lock.lock()
            runRobot2.signal()
            lock.unlock()
        }

    }

    fun stopRobot() {
        runRobot.set(false)
    }

    val isRunning: Boolean
        get() = runRobot.get()
    
    override fun run() {
        println("Robot starting")

        while (Thread.currentThread().isInterrupted == false) {
            subconscThread.start()

            while (runRobot.get() == true && Thread.currentThread().isInterrupted == false) {
                if (Thread.currentThread().isInterrupted == true) {
                    runRobot.set(false)
                } else {


                    // pass the latest sweep to the feature finder algorithm, then wait a bit for now...


                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
            }

            // shut down our subconscious:
            subconscThread.interrupt()
            try {
                subconscThread.join()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            // wait until notified that the robot should start:
            lock.lock()
            try {
                while (runRobot.get() == false && Thread.currentThread().isInterrupted == false) {
                    try {
                        runRobot2.await()
                    } catch (E: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            } finally {
                lock.unlock()
            }
        }
        println("Robot completed")
    }

}
