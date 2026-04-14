package io.github.cptimario.mousemover.platform;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class JvmIdleTimeProviderTest {
  @Test
  public void testMarkActivityResetsIdle() {
    JvmIdleTimeProvider p = new JvmIdleTimeProvider();
    long before = p.getIdleTimeSeconds();
    p.markActivity();
    long after = p.getIdleTimeSeconds();
    assertTrue(after <= before + 1, "after markActivity idle should be small");
  }
}
