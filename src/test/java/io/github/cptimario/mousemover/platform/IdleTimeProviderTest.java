package io.github.cptimario.mousemover.platform;

import org.junit.jupiter.api.Test;

public class IdleTimeProviderTest {

  @Test
  public void testDefaultMarkActivity_noOp() {
    IdleTimeProvider p =
        new IdleTimeProvider() {
          @Override
          public long getIdleTimeSeconds() {
            return 0;
          }
        };

    // should be a no-op and not throw
    p.markActivity();
  }
}
