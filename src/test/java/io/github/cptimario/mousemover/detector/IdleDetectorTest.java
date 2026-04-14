package io.github.cptimario.mousemover.detector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cptimario.mousemover.platform.IdleTimeProvider;
import java.awt.*;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class IdleDetectorTest {

  @Test
  public void testIdleBelowThreshold() {
    // provider reports 10 seconds idle
    IdleTimeProvider provider = () -> 10L;
    IdleDetector detector = new IdleDetector(provider, 30, 5, 5, false, 50, false, new Random(0));
    Instant lastMove = Instant.now().minusSeconds(1000);
    Point pos = new Point(500, 500);
    Dimension screen = new Dimension(1000, 1000);

    IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
    assertFalse(d.shouldMove(), "Should not move when idle time is below threshold");
  }

  @Test
  public void testGraceWindowSkipsAndAllows() {
    // deterministic Random that always returns 3 for nextInt(bound)
    Random fixed =
        new Random() {
          @Override
          public int nextInt(int bound) {
            return Math.clamp(bound - 1, 0, 3);
          }
        };

    // provider that reports a configurable idle time via closure
    record P(long v) implements IdleTimeProvider {

      @Override
      public long getIdleTimeSeconds() {
        return v;
      }
    }

    IdleDetector detector = new IdleDetector(new P(7), 5, 5, 1, false, 50, false, fixed);
    Instant lastMove = Instant.now().minusSeconds(1000);
    Point pos = new Point(500, 500);
    Dimension screen = new Dimension(1000, 1000);

    // idleTime = 7 (< 5 + 3) -> skip
    IdleDetector.IdleDecision d1 = detector.evaluate(lastMove, pos, screen);
    assertFalse(d1.shouldMove(), "Should skip within randomized grace window");

    // idleTime = 8 (== 5 + 3) -> allow
    detector = new IdleDetector(new P(8), 5, 5, 1, false, 50, false, fixed);
    IdleDetector.IdleDecision d2 = detector.evaluate(lastMove, pos, screen);
    assertTrue(d2.shouldMove(), "Should allow when idleTime meets idle+grace");
  }

  @Test
  public void testCooldownSkips() {
    IdleTimeProvider provider = () -> 100L;
    IdleDetector detector = new IdleDetector(provider, 5, 0, 10, false, 50, false, new Random(0));
    Instant lastMove = Instant.now(); // just moved
    Point pos = new Point(500, 500);
    Dimension screen = new Dimension(1000, 1000);

    IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
    assertFalse(d.shouldMove(), "Should skip due to cooldown since last move");
  }

  @Test
  public void testEdgeSuppression() {
    IdleTimeProvider provider = () -> 100L;
    IdleDetector detector = new IdleDetector(provider, 5, 0, 1, false, 50, false, new Random(0));
    Instant lastMove = Instant.now().minusSeconds(1000);
    // position near left edge
    Point pos = new Point(10, 500);
    Dimension screen = new Dimension(1000, 1000);

    IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
    assertFalse(d.shouldMove(), "Should skip when mouse is near edge");
  }
}
