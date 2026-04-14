package io.github.cptimario.mousemover.core;

import io.github.cptimario.mousemover.detector.IdleDetector;
import io.github.cptimario.mousemover.platform.IdleTimeProviderFactory;
import io.github.cptimario.mousemover.platform.JvmIdleTimeProvider;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Core service that performs the periodic idle checks and moves the mouse. */
public class MouseMoverService {
  private static final Logger logger = LoggerFactory.getLogger(MouseMoverService.class);
  private final Random random;

  private final int idleSeconds;
  private final int intervalSeconds;
  private final int graceSeconds;
  private final int jitter;
  private final boolean fullscreenDetection;
  private final boolean micro;
  private final int edgeMargin;
  private final boolean verbose;

  private volatile boolean running = false;
  private Point lastMousePosition;
  private Instant lastMovementAttempt = Instant.MIN;
  private IdleDetector detector;

  private final CountDownLatch stopLatch;
  private ScheduledExecutorService executor;

  public MouseMoverService(
      final int idleSeconds,
      final int intervalSeconds,
      final int graceSeconds,
      final int jitter,
      final int edgeMargin,
      final boolean fullscreenDetection,
      final boolean micro,
      final boolean verbose) {
    this.idleSeconds = idleSeconds;
    this.intervalSeconds = intervalSeconds;
    this.graceSeconds = Math.max(0, graceSeconds);
    this.jitter = jitter;
    this.edgeMargin = Math.max(0, edgeMargin);
    this.fullscreenDetection = fullscreenDetection;
    this.micro = micro;
    this.verbose = verbose;
    // initialize fields that might interact with system state in constructor
    this.random = new Random();
    this.stopLatch = new CountDownLatch(1);
    try {
      this.lastMousePosition = MouseInfo.getPointerInfo().getLocation();
    } catch (Throwable t) {
      this.lastMousePosition = new Point(0, 0);
    }
  }

  /** Package-private constructor used for testing to inject deterministic collaborators. */
  MouseMoverService(
      final int idleSeconds,
      final int intervalSeconds,
      final int graceSeconds,
      final int jitter,
      final int edgeMargin,
      final boolean fullscreenDetection,
      final boolean micro,
      final boolean verbose,
      final Random random,
      final Point initialMousePosition,
      final IdleDetector detector) {
    this.idleSeconds = idleSeconds;
    this.intervalSeconds = intervalSeconds;
    this.graceSeconds = Math.max(0, graceSeconds);
    this.jitter = jitter;
    this.edgeMargin = Math.max(0, edgeMargin);
    this.fullscreenDetection = fullscreenDetection;
    this.micro = micro;
    this.verbose = verbose;
    this.random = (random == null) ? new Random() : random;
    this.stopLatch = new CountDownLatch(1);
    this.lastMousePosition =
        (initialMousePosition == null) ? new Point(0, 0) : initialMousePosition;
    this.detector = detector;
  }

  public void start() {
    try {
      Robot robot = createRobot();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      final JvmIdleTimeProvider fallback = new JvmIdleTimeProvider();
      final var provider = IdleTimeProviderFactory.create(fallback);
      detector =
          new IdleDetector(
              provider,
              idleSeconds,
              graceSeconds,
              intervalSeconds,
              fullscreenDetection,
              edgeMargin,
              micro,
              random);

      executor = Executors.newSingleThreadScheduledExecutor();
      MouseRobot robotWrap = new AwtMouseRobot(robot);
      executor.scheduleAtFixedRate(
          () -> checkIdleAndMove(robotWrap, screenSize), 0, intervalSeconds, TimeUnit.SECONDS);

      running = true;
      logger.info("Mouse mover service started");
    } catch (AWTException e) {
      logger.error("Failed to start MouseMoverService: {}", e.getMessage(), e);
      stopLatch.countDown();
    }
  }

  /**
   * Package-private helper to create a Robot; extracted for testing so tests can override and
   * simulate AWT failures.
   */
  Robot createRobot() throws AWTException {
    return new Robot();
  }

  /**
   * Start the service using a provided test-friendly {@link MouseRobot} and screen size. This is
   * package-private and intended for tests so we can avoid creating a real {@link Robot}.
   */
  void startWithRobot(MouseRobot robotWrap, Dimension screenSize) {
    try {
      final JvmIdleTimeProvider fallback = new JvmIdleTimeProvider();
      final var provider = IdleTimeProviderFactory.create(fallback);
      if (detector == null) {
        detector =
            new IdleDetector(
                provider,
                idleSeconds,
                graceSeconds,
                intervalSeconds,
                fullscreenDetection,
                edgeMargin,
                micro,
                random);
      }

      executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleAtFixedRate(
          () -> checkIdleAndMove(robotWrap, screenSize), 0, intervalSeconds, TimeUnit.SECONDS);

      running = true;
      logger.info("Mouse mover service started");
    } catch (Exception e) {
      logger.error("Failed to start MouseMoverService: {}", e.getMessage(), e);
      stopLatch.countDown();
    }
  }

  public void stop() {
    running = false;
    if (executor != null) executor.shutdownNow();
    stopLatch.countDown();
    logger.info("Mouse mover service stopped");
  }

  public void join() {
    try {
      stopLatch.await();
    } catch (InterruptedException ignored) {
    }
  }

  void checkIdleAndMove(MouseRobot robot, Dimension screenSize) {
    Point currentMousePosition = MouseInfo.getPointerInfo().getLocation();
    if (!currentMousePosition.equals(lastMousePosition)) {
      detector.notifyActivity();
      lastMousePosition = currentMousePosition;
      if (verbose) {
        logger.debug("External mouse movement detected, resetting idle timer.");
      }
    }

    IdleDetector.IdleDecision decision =
        detector.evaluate(lastMovementAttempt, lastMousePosition, screenSize);
    if (decision.shouldMove()) {
      if (verbose) {
        logger.debug("IdleDetector decision: move (reason={})", decision.reason());
      }
      moveMouseHumanLike(robot, screenSize);
      lastMovementAttempt = Instant.now();
      detector.notifyActivity();
      lastMousePosition = MouseInfo.getPointerInfo().getLocation();
    } else {
      if (verbose) {
        logger.debug("IdleDetector decision: skip (reason={})", decision.reason());
      }
    }
  }

  void moveMouseHumanLike(MouseRobot robot, Dimension screenSize) {
    if (micro) {
      final int dx = random.nextInt(5) - 2;
      final int dy = random.nextInt(5) - 2;
      final int nx = Math.max(0, Math.min(screenSize.width - 1, lastMousePosition.x + dx));
      final int ny = Math.max(0, Math.min(screenSize.height - 1, lastMousePosition.y + dy));
      if (verbose) {
        logger.debug(
            "Micro-jitter moving mouse from ({},{}) to ({},{})",
            lastMousePosition.x,
            lastMousePosition.y,
            nx,
            ny);
      }
      robot.mouseMove(nx, ny);
      try {
        robot.sleepMillis(10 + random.nextInt(40));
      } catch (InterruptedException ignored) {
      }
      return;
    }

    int targetX = random.nextInt(screenSize.width);
    int targetY = random.nextInt(screenSize.height);

    int startX = lastMousePosition.x;
    int startY = lastMousePosition.y;
    int steps = 30 + random.nextInt(40);
    int dx = (targetX - startX) / steps;
    int dy = (targetY - startY) / steps;

    if (verbose) {
      logger.debug(
          "Moving mouse from ({},{}) to ({},{}) in {} steps",
          startX,
          startY,
          targetX,
          targetY,
          steps);
    }

    for (int i = 0; i < steps; i++) {
      startX += dx + (random.nextInt(2 * jitter + 1) - jitter);
      startY += dy + (random.nextInt(2 * jitter + 1) - jitter);
      robot.mouseMove(startX, startY);
      try {
        robot.sleepMillis(10 + random.nextInt(30));
      } catch (InterruptedException ignored) {
      }
    }
  }
}
