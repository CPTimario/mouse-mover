package io.github.cptimario.mousemover.platform.nativeimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.cptimario.mousemover.platform.IdleTimeProvider;
import io.github.cptimario.mousemover.platform.IdleTimeProviderFactory;
import io.github.cptimario.mousemover.platform.JvmIdleTimeProvider;
import org.junit.jupiter.api.Test;

public class MacOSIdleTimeProviderTest {
  @Test
  public void testFactoryFallsBackToJvmWhenNativeUnavailable() {
    IdleTimeProvider fallback = new JvmIdleTimeProvider();
    IdleTimeProvider provider = IdleTimeProviderFactory.create((JvmIdleTimeProvider) fallback);
    assertEquals(0L, provider.getIdleTimeSeconds());
  }
}
