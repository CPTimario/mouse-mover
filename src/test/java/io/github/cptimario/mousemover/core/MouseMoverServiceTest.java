package io.github.cptimario.mousemover.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cptimario.mousemover.detector.IdleDetector;
import io.github.cptimario.mousemover.platform.JvmIdleTimeProvider;
import io.github.cptimario.mousemover.testutil.TestMouseRobot;
import java.awt.*;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class MouseMoverServiceTest {

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

  static class NeverMoveDetector extends IdleDetector {
    public NeverMoveDetector() {
      super(new JvmIdleTimeProvider(), 1, 0, 1, false, 50, false, new Random(0));
    }

    @Override
    public IdleDecision evaluate(
        Instant lastMovementAttempt, Point lastMousePosition, Dimension screenSize) {
      return new IdleDecision(false, "test");
    }
  }

  @Test
  public void testCheckIdleAndMove_invokesMouseMoveWhenDetectorSaysMove() {
    TestMouseRobot robot = new TestMouseRobot();
    AlwaysMoveDetector d = new AlwaysMoveDetector();
    MouseMoverService service =
        new MouseMoverService(
            5, 1, 0, 0, 50, false, false, false, new Random(0), new Point(100, 100), d);

    Dimension screen = new Dimension(800, 600);
    service.checkIdleAndMove(robot, screen);
    assertTrue(robot.getMoves().size() > 0, "robot should have recorded at least one move");
  }

  @Test
  public void testCheckIdleAndMove_skipsWhenDetectorSaysSkip() {
    TestMouseRobot robot = new TestMouseRobot();
    NeverMoveDetector d = new NeverMoveDetector();
    MouseMoverService service =
        new MouseMoverService(
            5, 1, 0, 0, 50, false, false, false, new Random(0), new Point(100, 100), d);

    Dimension screen = new Dimension(800, 600);
    service.checkIdleAndMove(robot, screen);
    assertFalse(robot.getMoves().size() > 0, "robot should not have moved");
  }
}
