package io.github.cptimario.mousemover.platform;

/** Abstraction for obtaining system idle time. */
public interface IdleTimeProvider {
  /**
   * @return idle time in seconds
   */
  long getIdleTimeSeconds();

  /** Notify the provider that activity occurred (keyboard/mouse). Default no-op. */
  default void markActivity() {}
}
