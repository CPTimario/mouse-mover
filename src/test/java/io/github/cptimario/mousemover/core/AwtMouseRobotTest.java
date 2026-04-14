package io.github.cptimario.mousemover.core;

import static org.mockito.Mockito.verify;

import java.awt.Robot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AwtMouseRobotTest {

  @Test
  public void testMouseMoveDelegatesToRobot() {
    Robot mockRobot = Mockito.mock(Robot.class);
    AwtMouseRobot awt = new AwtMouseRobot(mockRobot);

    awt.mouseMove(10, 20);

    verify(mockRobot).mouseMove(10, 20);
  }

  @Test
  public void testSleepMillis_completes() throws Exception {
    Robot mockRobot = Mockito.mock(Robot.class);
    AwtMouseRobot awt = new AwtMouseRobot(mockRobot);

    // should not throw for a very short sleep
    awt.sleepMillis(1);
  }
}
