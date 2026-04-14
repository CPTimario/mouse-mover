package io.github.cptimario.mousemover.detector;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.cptimario.mousemover.platform.IdleTimeProvider;
import java.awt.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class IdleDetectorFullscreenTest {

  @Test
  public void testFullscreenDetectionSkipsWhenMatchesScreenBounds() {
    IdleTimeProvider provider = () -> 100L;
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    IdleDetector detector = new IdleDetector(provider, 5, 0, 1, true, 50, false, null);
    Instant lastMove = Instant.now().minusSeconds(1000);
    Point pos = new Point(100, 100);

    IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
    assertFalse(d.shouldMove(), "Should skip when fullscreen detected");
  }
}
