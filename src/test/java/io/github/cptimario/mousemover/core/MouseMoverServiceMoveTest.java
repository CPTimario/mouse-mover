package io.github.cptimario.mousemover.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cptimario.mousemover.detector.IdleDetector;
import io.github.cptimario.mousemover.platform.JvmIdleTimeProvider;
import io.github.cptimario.mousemover.testutil.TestMouseRobot;
import java.awt.*;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class MouseMoverServiceMoveTest {

  // Fixed random that returns a repeating sequence of ints (modulo bound) to make moves
  // deterministic
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
      // ensure non-negative and within bound
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
  public void testMoveMouseHumanLike_micro_movement() {
    // micro=true path: random.nextInt(5) -> dx/dy, and nextInt(40) used for sleep; TestMouseRobot
    // no-ops sleep
    FixedRandom rnd = new FixedRandom(2, 3, 5); // values will be modded as needed
    TestMouseRobot robot = new TestMouseRobot();
    MouseMoverService svc =
        new MouseMoverService(
            5, 1, 0, 1, 50, false, true, true, rnd, new Point(100, 100), new AlwaysMoveDetector());

    Dimension screen = new Dimension(800, 600);
    // Call moveMouseHumanLike directly to exercise micro path
    svc.moveMouseHumanLike(robot, screen);

    // micro moves should produce exactly one mouseMove call
    assertEquals(1, robot.getMoves().size());
  }

  @Test
  public void testMoveMouseHumanLike_large_movement_deterministic() {
    // Provide a deterministic random: targetX=1, targetY=1, stepsOffset=0, jitter returns 0
    // repeatedly
    FixedRandom rnd = new FixedRandom(1, 1, 0, 0, 0, 0);
    TestMouseRobot robot = new TestMouseRobot();
    MouseMoverService svc =
        new MouseMoverService(
            5, 1, 0, 1, 50, false, false, true, rnd, new Point(10, 10), new AlwaysMoveDetector());

    Dimension screen = new Dimension(100, 100);
    // Call moveMouseHumanLike directly to exercise non-micro path
    svc.moveMouseHumanLike(robot, screen);

    // steps = 30 + nextInt(40) -> 30 (since our seq returns 0 for that call), so expect 30 moves
    assertTrue(robot.getMoves().size() >= 30, "expected at least 30 moves");
  }

  @Test
  public void testStopAndJoin_doNotBlock() throws Exception {
    MouseMoverService svc =
        new MouseMoverService(
            5, 1, 0, 0, 50, false, false, false, new Random(0), new Point(0, 0), null);
    // stopping should count down latch and join should return
    svc.stop();
    svc.join();
  }
}
