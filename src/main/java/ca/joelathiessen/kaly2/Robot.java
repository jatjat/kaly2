package ca.joelathiessen.kaly2;

import ca.joelathiessen.kaly2.subconscious.Measurement;
import ca.joelathiessen.kaly2.subconscious.Subconscious;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Robot implements Runnable {

  private Subconscious subconsc;
  private Thread subconscThread;
  private AtomicBoolean runRobot = new AtomicBoolean(false);
  private ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps;

  public Robot(Subconscious subconsc, ConcurrentLinkedQueue<ArrayList<Measurement>> sweeps) {
    this.subconsc = subconsc;
    this.sweeps = sweeps;
    subconscThread = new Thread(subconsc);
  }

  public synchronized void startRobot() {
    if (runRobot.get() == false) {
      runRobot.set(true);
      this.notify();
    }
  }

  public void stopRobot() {
    runRobot.set(false);
  }


  public void run() {
    System.out.println("Robot starting");
    boolean interrupted = false;

    while (interrupted == false) {

      if (runRobot.get() == true) {
        subconscThread.start();

        while (runRobot.get() == true) {
          if (Thread.currentThread().isInterrupted() == true) {
            interrupted = true;
            runRobot.set(false);
          } else {


            // pass the latest sweep to the feature finder algorithm, then wait a bit...
            
            
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
              synchronized (this) {
                Thread.currentThread().interrupt();
              }
            }
          }
        }


        // shut down our subconscious:
        subconscThread.interrupt();
        try {
          subconscThread.join();
        } catch (InterruptedException e) {
          synchronized (this) {
            Thread.currentThread().interrupt();
          }
        }
      }

      // wait until notified that the robot should start:
      if (interrupted == false) {
        try {
          synchronized (this) {
            this.wait();
          }
        } catch (InterruptedException e) {
          interrupted = true;
          runRobot.set(false);
        }
      }
    }
    System.out.println("Robot completed");
  }

  public boolean isRunning() {
    return runRobot.get();
  }


}
