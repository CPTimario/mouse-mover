package io.github.cptimario.mousemover.platform;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cptimario.mousemover.platform.nativeimpl.MacOSIdleTimeProvider;
import io.github.cptimario.mousemover.platform.nativeimpl.WindowsIdleTimeProvider;
import org.junit.jupiter.api.Test;

public class IdleTimeProviderFactoryTest {

  @Test
  public void testCreate_macAndWindowsFallback() {
    String orig = System.getProperty("os.name");
    try {
      System.setProperty("os.name", "Mac OS X");
      IdleTimeProvider p = IdleTimeProviderFactory.create(new JvmIdleTimeProvider());
      assertTrue(p instanceof MacOSIdleTimeProvider || p instanceof JvmIdleTimeProvider);

      System.setProperty("os.name", "Windows 10");
      p = IdleTimeProviderFactory.create(new JvmIdleTimeProvider());
      assertTrue(p instanceof WindowsIdleTimeProvider || p instanceof JvmIdleTimeProvider);
    } finally {
      if (orig == null) System.clearProperty("os.name");
      else System.setProperty("os.name", orig);
    }
  }
}
