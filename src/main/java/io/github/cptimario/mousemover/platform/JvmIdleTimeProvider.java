package io.github.cptimario.mousemover.platform;

import java.time.Duration;
import java.time.Instant;

/** Pure-Java fallback implementation that tracks activity via markActivity(). */
public class JvmIdleTimeProvider implements IdleTimeProvider {
  private volatile Instant lastActivity = Instant.now();

  @Override
  public long getIdleTimeSeconds() {
    return Duration.between(lastActivity, Instant.now()).toSeconds();
  }

  @Override
  public void markActivity() {
    lastActivity = Instant.now();
  }
}
