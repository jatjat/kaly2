package ca.joelathiessen.kaly2.main;

import ca.joelathiessen.kaly2.main.subconscious.Subconscious;

public class Robot {

  private Subconscious subconsc;
  private Thread subconscThread;

  public Robot(Subconscious subconsc) {
    this.subconsc = subconsc;
    subconscThread = new Thread(subconsc);
  }

  public void start() {
    System.out.println("Robot starting");
    subconscThread.start();
  }

}
