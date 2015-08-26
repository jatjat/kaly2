package ca.joelathiessen.kaly2.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Commander {

  private BufferedReader br;
  private Thread robotThread;
  private Robot robot;


  public Commander(Robot robot) {
    robotThread = new Thread(robot);
    this.robot = robot;
    br = new BufferedReader(new InputStreamReader(System.in));

  }

  public void takeCommands() {
    String input;
    boolean quit = false;
    System.out.println("Enter a command (q to quit):");
    robotThread.start();
    try {

      while (quit == false) {
        input = br.readLine();
        if (input == null || input.equals("q")) {

          quit = true;
        } else if (input.equals("g")) {
          robot.startRobot();
        } else if (input.equals("r")) {
          robot.stopRobot();
        }
      }
      robot.stopRobot();

    } catch (IOException io) {
      io.printStackTrace();
    }
    robotThread.interrupt();
    
    try {
      robotThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Program successfully completed");
  }

  public static void main(String[] args) {
    // the robot will start from here; i.e. :
    // Commander commander = new Commander(robot);
  }

}
