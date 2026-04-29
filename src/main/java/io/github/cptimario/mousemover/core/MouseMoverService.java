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
      // Wrap the scheduled task in a top-level try/catch so an unexpected runtime exception
      // (for example a SecurityException from Robot.mouseMove on macOS when Accessibility is
      // revoked) does not terminate the scheduled task permanently. Log and continue.
      executor.scheduleAtFixedRate(
          () -> {
            try {
              checkIdleAndMove(robotWrap, screenSize);
            } catch (Throwable t) {
              logger.error(
                  "Unhandled exception in scheduled idle-check/move task: {}", t.getMessage(), t);
            }
          },
          0,
          intervalSeconds,
          TimeUnit.SECONDS);

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

      logger.info("Mouse mover service started");
    } catch (Exception e) {
      logger.error("Failed to start MouseMoverService: {}", e.getMessage(), e);
      stopLatch.countDown();
    }
  }

  public void stop() {
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
    Point currentMousePosition;
    try {
      currentMousePosition = MouseInfo.getPointerInfo().getLocation();
    } catch (Throwable t) {
      // In headless environments MouseInfo may throw HeadlessException. Fall back to the
      // last known mouse position so tests and headless CI do not fail.
      currentMousePosition = (lastMousePosition == null) ? new Point(0, 0) : lastMousePosition;
    }

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
      // Attempt to read the system pointer again, but don't fail if unavailable
      try {
        lastMousePosition = MouseInfo.getPointerInfo().getLocation();
      } catch (Throwable t) {
        // In headless/test environments, preserve the previous position
        // (move calls are recorded by the injected MouseRobot in tests)
        // leaving lastMousePosition as-is is acceptable.
      }
    } else {
      if (verbose) {
        logger.debug("IdleDetector decision: skip (reason={})", decision.reason());
      }
    }
  }

  void moveMouseHumanLike(MouseRobot robot, Dimension screenSize) {
    // Compute a safe rectangle inside the configured edge margin. If the configured
    // edge margin leaves no safe area, fall back to the screen center (Option A).
    final int safeLeft = edgeMargin;
    final int safeTop = edgeMargin;
    final int safeRight = screenSize.width - 1 - edgeMargin;
    final int safeBottom = screenSize.height - 1 - edgeMargin;
    final boolean hasSafeArea = safeRight >= safeLeft && safeBottom >= safeTop;
    final int fallbackX = screenSize.width / 2;
    final int fallbackY = screenSize.height / 2;

    if (micro) {
      final int dx = random.nextInt(5) - 2;
      final int dy = random.nextInt(5) - 2;
      int nx = lastMousePosition.x + dx;
      int ny = lastMousePosition.y + dy;
      if (hasSafeArea) {
        nx = Math.clamp(nx, safeLeft, safeRight);
        ny = Math.clamp(ny, safeTop, safeBottom);
      } else {
        // No safe area: move to fallback center to avoid repeatedly placing pointer
        // in edge-suppressed regions.
        nx = fallbackX;
        ny = fallbackY;
      }
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

    final int targetX;
    final int targetY;
    if (hasSafeArea) {
      targetX = safeLeft + random.nextInt(safeRight - safeLeft + 1);
      targetY = safeTop + random.nextInt(safeBottom - safeTop + 1);
    } else {
      targetX = fallbackX;
      targetY = fallbackY;
    }

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
      // Clamp each intermediate step into the safe area (or keep within screen if
      // no safe area is available — though in that case target was the center).
      if (hasSafeArea) {
        startX = Math.max(safeLeft, Math.min(safeRight, startX));
        startY = Math.max(safeTop, Math.min(safeBottom, startY));
      } else {
        startX = Math.max(0, Math.min(screenSize.width - 1, startX));
        startY = Math.max(0, Math.min(screenSize.height - 1, startY));
      }
      robot.mouseMove(startX, startY);
      try {
        robot.sleepMillis(10 + random.nextInt(30));
      } catch (InterruptedException ignored) {
      }
    }
    // Log a high-level info message when a move completes so operators can see activity in
    // production logs without DEBUG enabled.
    try {
      logger.debug("Moved mouse to ({},{})", startX, startY);
    } catch (Throwable ignored) {
    }
  }
}
