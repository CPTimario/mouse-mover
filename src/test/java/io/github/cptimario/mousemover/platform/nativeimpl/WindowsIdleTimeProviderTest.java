package io.github.cptimario.mousemover.platform.nativeimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class WindowsIdleTimeProviderTest {
  @Test
  public void testGetIdleTimeReturnsZeroWhenNativeUnavailable() {
    WindowsIdleTimeProvider p = new WindowsIdleTimeProvider();
    assertEquals(0L, p.getIdleTimeSeconds());
  }
}
