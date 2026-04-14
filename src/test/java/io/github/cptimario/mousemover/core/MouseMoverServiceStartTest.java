package io.github.cptimario.mousemover.core;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.cptimario.mousemover.detector.IdleDetector;
import io.github.cptimario.mousemover.platform.JvmIdleTimeProvider;
import io.github.cptimario.mousemover.testutil.TestMouseRobot;
import java.awt.*;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class MouseMoverServiceStartTest {

  static class AlwaysMoveDetector extends IdleDetector {
    public AlwaysMoveDetector() {
      super(new JvmIdleTimeProvider(), 1, 0, 1, false, 50, false, new Random(0));
    }

    @Override
    public IdleDecision evaluate(
        Instant lastMovementAttempt, Point lastMousePosition, Dimension screenSize) {
      return new IdleDecision(true, "test");
    }
  }

  @Test
  public void testStartWithRobot_runsScheduledTask() throws Exception {
    TestMouseRobot robot = new TestMouseRobot();
    MouseMoverService svc =
        new MouseMoverService(
            1,
            1,
            0,
            1,
            10,
            false,
            false,
            false,
            new Random(0),
            new Point(0, 0),
            new AlwaysMoveDetector());

    svc.startWithRobot(robot, new Dimension(20, 20));
    // allow scheduled task to run at least once (poll up to 2s for a move to appear)
    long deadline = System.currentTimeMillis() + 2000;
    while (System.currentTimeMillis() < deadline && robot.getMoves().isEmpty()) {
      Thread.sleep(50);
    }
    svc.stop();
    svc.join();

    assertFalse(robot.getMoves().isEmpty(), "scheduled task should have invoked mouse moves");
  }

  @Test
  public void testStart_handlesAWTExceptionAndCountsDownLatch() throws Exception {
    MouseMoverService svc =
        new MouseMoverService(
            1, 1, 0, 1, 10, false, false, false, new Random(0), new Point(0, 0), null) {
          @Override
          Robot createRobot() throws AWTException {
            throw new AWTException("simulated");
          }
        };

    // start should catch AWTException and count down latch so join() returns
    svc.start();
    svc.join();
  }
}
