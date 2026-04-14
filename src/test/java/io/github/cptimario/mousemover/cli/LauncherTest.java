package io.github.cptimario.mousemover.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.cptimario.mousemover.core.MouseMoverService;
import org.junit.jupiter.api.Test;

public class LauncherTest {

  @Test
  public void testCall_usesCreateServiceHook() throws Exception {
    Launcher l =
        new Launcher() {
          @Override
          MouseMoverService createService() {
            // return a no-op subclass using the public constructor so we don't need package access
            return new MouseMoverService(
                idleSeconds,
                intervalSeconds,
                graceSeconds,
                jitter,
                edgeMargin,
                fullscreenDetection,
                micro,
                verbose) {
              @Override
              public void start() {}

              @Override
              public void join() {}

              @Override
              public void stop() {}
            };
          }
        };

    assertEquals(0, l.call().intValue());
  }
}
