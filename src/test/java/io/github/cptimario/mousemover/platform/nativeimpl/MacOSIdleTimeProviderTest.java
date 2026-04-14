package io.github.cptimario.mousemover.platform.nativeimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MacOSIdleTimeProviderTest {
  @Test
  public void testGetIdleTimeReturnsZeroWhenNativeUnavailable() {
    MacOSIdleTimeProvider p = new MacOSIdleTimeProvider();
    assertEquals(0L, p.getIdleTimeSeconds());
  }
}
