package io.github.cptimario.mousemover.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.cptimario.mousemover.core.MouseMoverService;
import org.junit.jupiter.api.Test;

public class LauncherCreateAndMainTest {

  @Test
  public void testCreateService_defaultInstance() {
    Launcher l = new Launcher();
    // calling the hook should return a non-null service instance
    assertEquals(MouseMoverService.class, l.createService().getClass());
  }

  @Test
  public void testMain_systemExitIsCalled() {
    // call the test-friendly entrypoint that returns exit code instead of exiting
    int rc = Launcher.executeForTests(new String[0]);
    assertEquals(0, rc);
  }
}
