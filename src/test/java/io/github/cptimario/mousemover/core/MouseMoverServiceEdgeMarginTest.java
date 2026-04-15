package io.github.cptimario.mousemover.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cptimario.mousemover.detector.IdleDetector;
import io.github.cptimario.mousemover.platform.JvmIdleTimeProvider;
import io.github.cptimario.mousemover.testutil.TestMouseRobot;
import java.awt.Dimension;
import java.awt.Point;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class MouseMoverServiceEdgeMarginTest {

  // Deterministic random used in tests
  static class FixedRandom extends Random {
    private final int[] seq;
    private int idx = 0;

    FixedRandom(int... seq) {
      this.seq = seq.length == 0 ? new int[] {0} : seq;
    }

    @Override
    public int nextInt(int bound) {
      int v = seq[idx % seq.length];
      idx++;
      if (bound <= 0) return 0;
      return Math.abs(v) % bound;
    }
  }

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
  public void testMicroMovementRespectsEdgeMargin() {
    int edgeMargin = 20;
    FixedRandom rnd = new FixedRandom(2, 3, 5);
    TestMouseRobot robot = new TestMouseRobot();
    MouseMoverService svc =
        new MouseMoverService(
            5,
            1,
            0,
            1,
            edgeMargin,
            false,
            true,
            true,
            rnd,
            new Point(50, 50),
            new AlwaysMoveDetector());

    Dimension screen = new Dimension(200, 100);
    svc.moveMouseHumanLike(robot, screen);

    assertFalse(robot.getMoves().isEmpty(), "expected at least one move");
    int[] m = robot.getMoves().get(0);
    int x = m[0];
    int y = m[1];

    int safeLeft = edgeMargin;
    int safeTop = edgeMargin;
    int safeRight = screen.width - 1 - edgeMargin;
    int safeBottom = screen.height - 1 - edgeMargin;

    assertTrue(x >= safeLeft && x <= safeRight, "x within safe horizontal bounds");
    assertTrue(y >= safeTop && y <= safeBottom, "y within safe vertical bounds");
  }

  @Test
  public void testLargeMovementRespectsEdgeMargin() {
    int edgeMargin = 30;
    // produce zeros so targets are at the left/top of the safe area and jitter is zero
    FixedRandom rnd = new FixedRandom(0, 0, 0, 0, 0, 0);
    TestMouseRobot robot = new TestMouseRobot();
    MouseMoverService svc =
        new MouseMoverService(
            5,
            1,
            0,
            1,
            edgeMargin,
            false,
            false,
            true,
            rnd,
            new Point(60, 40),
            new AlwaysMoveDetector());

    Dimension screen = new Dimension(200, 120);
    svc.moveMouseHumanLike(robot, screen);

    assertFalse(robot.getMoves().isEmpty(), "expected moves to be recorded");

    int safeLeft = edgeMargin;
    int safeTop = edgeMargin;
    int safeRight = screen.width - 1 - edgeMargin;
    int safeBottom = screen.height - 1 - edgeMargin;

    for (int[] m : robot.getMoves()) {
      int x = m[0];
      int y = m[1];
      assertTrue(x >= safeLeft && x <= safeRight, "x within safe horizontal bounds");
      assertTrue(y >= safeTop && y <= safeBottom, "y within safe vertical bounds");
    }
  }
}
