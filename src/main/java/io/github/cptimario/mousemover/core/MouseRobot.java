package io.github.cptimario.mousemover.core;

/** Abstraction over mouse actions to make movement and sleeps testable. */
public interface MouseRobot {
  void mouseMove(int x, int y);

  /** Sleep for the specified milliseconds. Test implementations may no-op. */
  void sleepMillis(long ms) throws InterruptedException;
}
