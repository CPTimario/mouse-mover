package io.github.cptimario.mousemover.platform;

/** Abstraction for obtaining system idle time. */
public interface IdleTimeProvider extends AutoCloseable {
  /**
   * @return idle time in seconds
   */
  long getIdleTimeSeconds();

  /** Notify the provider that activity occurred (keyboard/mouse). Default no-op. */
  default void markActivity() {}

  /** Release any resources (e.g. native hooks). Default no-op. */
  @Override
  default void close() {}
}
