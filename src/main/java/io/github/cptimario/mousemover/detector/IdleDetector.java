package io.github.cptimario.mousemover.detector;

import io.github.cptimario.mousemover.platform.IdleTimeProvider;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Random;

/**
 * Encapsulates idle heuristics used to decide whether to move the mouse. Pure logic (no Robot
 * calls) so it can be unit tested.
 */
public class IdleDetector {
  private final int idleSeconds;
  private final int graceSeconds;
  private final int intervalSeconds;
  private final boolean fullscreenDetection;
  private final int edgeMargin;
  private final boolean micro;
  private final Random random;
  private final IdleTimeProvider idleProvider;

  public IdleDetector(
      IdleTimeProvider idleProvider,
      int idleSeconds,
      int graceSeconds,
      int intervalSeconds,
      boolean fullscreenDetection,
      int edgeMargin,
      boolean micro,
      Random random) {
    this.idleProvider = Objects.requireNonNull(idleProvider, "idleProvider");
    this.idleSeconds = idleSeconds;
    this.graceSeconds = Math.max(0, graceSeconds);
    this.intervalSeconds = intervalSeconds;
    this.fullscreenDetection = fullscreenDetection;
    this.edgeMargin = Math.max(0, edgeMargin);
    this.micro = micro;
    this.random = (random == null) ? new Random() : random;
  }

  public IdleDecision evaluate(
      final Instant lastMovementAttempt,
      final Point lastMousePosition,
      final Dimension screenSize) {
    long idleTime = idleProvider.getIdleTimeSeconds();
    long sinceLastMove = Duration.between(lastMovementAttempt, Instant.now()).toSeconds();

    if (idleTime < idleSeconds) {
      return new IdleDecision(false, "idleTime=" + idleTime + " < idleSeconds=" + idleSeconds);
    }

    int grace = random.nextInt(graceSeconds + 1);
    if (idleTime < idleSeconds + grace) {
      return new IdleDecision(false, "within random grace=" + grace);
    }

    if (sinceLastMove <= intervalSeconds) {
      return new IdleDecision(
          false, "cooldown since last move=" + sinceLastMove + " <= interval=" + intervalSeconds);
    }

    if (fullscreenDetection && isLikelyFullscreen(screenSize)) {
      return new IdleDecision(false, "fullscreen-detected");
    }

    if (lastMousePosition != null && isNearEdge(lastMousePosition, screenSize)) {
      return new IdleDecision(false, "near-edge");
    }

    return new IdleDecision(true, micro ? "micro-move" : "move");
  }

  /**
   * Notify the detector that activity has occurred (keyboard/mouse). This delegates to the
   * provider.
   */
  public void notifyActivity() {
    try {
      idleProvider.markActivity();
    } catch (Throwable ignored) {
    }
  }

  boolean isLikelyFullscreen(Dimension screenSize) {
    try {
      Rectangle bounds =
          GraphicsEnvironment.getLocalGraphicsEnvironment()
              .getDefaultScreenDevice()
              .getDefaultConfiguration()
              .getBounds();
      return bounds.width == screenSize.width && bounds.height == screenSize.height;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isNearEdge(Point p, Dimension screen) {
    int margin = edgeMargin;
    return p.x < margin
        || p.y < margin
        || p.x > screen.width - margin
        || p.y > screen.height - margin;
  }

  public record IdleDecision(boolean shouldMove, String reason) {}
}
