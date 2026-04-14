package io.github.cptimario.mousemover.testutil;

import io.github.cptimario.mousemover.core.MouseRobot;
import java.util.ArrayList;
import java.util.List;

/** Test helper that records mouseMove calls and no-ops sleeps. */
public class TestMouseRobot implements MouseRobot {
  private final List<int[]> moves = new ArrayList<>();

  @Override
  public void mouseMove(int x, int y) {
    moves.add(new int[] {x, y});
  }

  @Override
  public void sleepMillis(long ms) {
    // no-op for tests
  }

  public List<int[]> getMoves() {
    return moves;
  }
}
