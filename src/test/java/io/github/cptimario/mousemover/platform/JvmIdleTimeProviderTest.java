package io.github.cptimario.mousemover.platform;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.junit.jupiter.api.Test;

public class JvmIdleTimeProviderTest {
  @Test
  public void testMarkActivityResetsIdle() throws Exception {
    try (JvmIdleTimeProvider p = new JvmIdleTimeProvider()) {
      long before = p.getIdleTimeSeconds();
      p.markActivity();
      long after = p.getIdleTimeSeconds();
      assertTrue(after <= before + 1, "after markActivity idle should be small");
    }
  }

  @Test
  public void testKeyPressResetsIdle() throws Exception {
    try (JvmIdleTimeProvider p = new JvmIdleTimeProvider()) {
      // Backdate activity so we can observe the listener resetting it.
      java.lang.reflect.Field f = JvmIdleTimeProvider.class.getDeclaredField("lastActivity");
      f.setAccessible(true);
      f.set(p, java.time.Instant.now().minusSeconds(120));
      assertTrue(p.getIdleTimeSeconds() >= 60, "precondition: should appear idle");

      NativeKeyEvent ev =
          new NativeKeyEvent(
              NativeKeyEvent.NATIVE_KEY_PRESSED,
              0,
              0,
              NativeKeyEvent.VC_A,
              NativeKeyEvent.CHAR_UNDEFINED);
      p.keyListener().nativeKeyPressed(ev);

      assertTrue(p.getIdleTimeSeconds() <= 1, "key press should reset idle to ~0");
    }
  }
}
