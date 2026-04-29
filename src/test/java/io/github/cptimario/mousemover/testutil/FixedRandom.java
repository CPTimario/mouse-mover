package io.github.cptimario.mousemover.testutil;

import java.util.Random;

/** Test helper that returns a repeating sequence of ints from {@link #nextInt(int)}. */
public class FixedRandom extends Random {
  private final int[] seq;
  private int idx = 0;

  public FixedRandom(int... seq) {
    this.seq = seq.length == 0 ? new int[] {0} : seq;
  }

  @Override
  public int nextInt(int bound) {
    int v = seq[idx % seq.length];
    idx++;
    if (bound <= 0) return 0;
    return Math.abs(v) % bound;
  }
}
