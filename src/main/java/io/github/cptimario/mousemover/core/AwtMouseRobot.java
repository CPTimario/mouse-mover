package io.github.cptimario.mousemover.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Robot;

/** Production MouseRobot that delegates to java.awt.Robot. */
public class AwtMouseRobot implements MouseRobot {
  private final Robot robot;

  /**
   * Wrap the provided {@link Robot} instance. This stores the provided reference intentionally; the
   * AWT {@link Robot} API is inherently mutable and this wrapper delegates directly to it. We
   * suppress the SpotBugs EI_EXPOSE_REP2 warning because callers (the service) intentionally create
   * and manage the Robot instance and tests rely on `MouseMoverService#createRobot()` being
   * overridable to simulate AWT failures.
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
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
