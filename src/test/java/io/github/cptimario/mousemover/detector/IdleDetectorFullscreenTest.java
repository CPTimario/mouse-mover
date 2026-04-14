package io.github.cptimario.mousemover.detector;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.cptimario.mousemover.platform.IdleTimeProvider;
import java.awt.Dimension;
import java.awt.Point;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class IdleDetectorFullscreenTest {

  @Test
  public void testFullscreenDetectionSkipsWhenMatchesScreenBounds() {
    // Avoid calling AWT Toolkit in tests (headless CI environments). Instead create an
    // IdleDetector subclass that forces fullscreen detection to true and pass a synthetic
    // screen size.
    IdleTimeProvider provider = () -> 100L;
    Dimension screen = new Dimension(1920, 1080);
    IdleDetector detector =
        new IdleDetector(provider, 5, 0, 1, true, 50, false, null) {
          @Override
          boolean isLikelyFullscreen(Dimension screenSize) {
            return true;
          }
        };
    Instant lastMove = Instant.now().minusSeconds(1000);
    Point pos = new Point(100, 100);

    IdleDetector.IdleDecision d = detector.evaluate(lastMove, pos, screen);
    assertFalse(d.shouldMove(), "Should skip when fullscreen detected");
  }
}
