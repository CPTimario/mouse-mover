package io.github.cptimario.mousemover.core;

import java.awt.Robot;

/** Production MouseRobot that delegates to java.awt.Robot. */
public class AwtMouseRobot implements MouseRobot {
  private final Robot robot;

  public AwtMouseRobot(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void mouseMove(int x, int y) {
    robot.mouseMove(x, y);
  }

  @Override
  public void sleepMillis(long ms) throws InterruptedException {
    Thread.sleep(ms);
  }
}
