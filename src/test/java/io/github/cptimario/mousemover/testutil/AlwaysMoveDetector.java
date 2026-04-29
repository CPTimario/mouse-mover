package io.github.cptimario.mousemover.testutil;

import io.github.cptimario.mousemover.detector.IdleDetector;
import io.github.cptimario.mousemover.platform.JvmIdleTimeProvider;
import java.awt.Dimension;
import java.awt.Point;
import java.time.Instant;
import java.util.Random;

/** Test helper that always signals the mouse should move. */
public class AlwaysMoveDetector extends IdleDetector {
  public AlwaysMoveDetector() {
    super(new JvmIdleTimeProvider(), 1, 0, 1, false, 50, false, new Random(0));
  }

  @Override
  public IdleDecision evaluate(
      Instant lastMovementAttempt, Point lastMousePosition, Dimension screenSize) {
    return new IdleDecision(true, "test");
  }
}
