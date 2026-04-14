package io.github.cptimario.mousemover;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.awt.*;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MouseMover implements Callable<Integer> {
    private static final Logger logger = Logger.getLogger(MouseMover.class.getName());
    private static final Random random = new Random();
    private static volatile boolean running = true;

    @Option(names = "--idle", description = "Idle threshold before moving the mouse (seconds)", defaultValue = "30")
    int idleSeconds;

    @Option(names = "--interval", description = "How often to check for idleness (seconds)", defaultValue = "5")
    int intervalSeconds;

    @Option(names = "--verbose", description = "Enable verbose logging (FINE level)")
    boolean verbose;

    @Option(names = "--jitter", description = "Max pixel jitter per step", defaultValue = "1")
    int jitter;

    @Option(names = "--grace", description = "Extra grace period after activity (seconds)", defaultValue = "5")
    int graceSeconds;

    @Option(names = "--fullscreen-detection", description = "Avoid moving mouse when fullscreen app is active")
    boolean fullscreenDetection = true;

    @Option(names = "--micro", description = "Use subtle micro-movements instead of large moves")
    boolean micro;

    @Option(names = "--edge-margin", description = "Pixel margin from screen edge to suppress movement", defaultValue = "50")
    int edgeMargin;

    private Point lastMousePosition = MouseInfo.getPointerInfo().getLocation();
    private Instant lastMovementAttempt = Instant.MIN;
    private IdleDetector detector;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MouseMover()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        configureLogging(verbose);

        try {
            registerNativeHook();
        } catch (NativeHookException ex) {
            logger.severe("Error registering native hook: " + ex.getMessage());
            return 1;
        }

        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Initialize IdleTimeProvider and IdleDetector with current options
        JvmIdleTimeProvider fallback = new JvmIdleTimeProvider();
        IdleTimeProvider provider = IdleTimeProviderFactory.create(fallback);
        detector = new IdleDetector(provider, idleSeconds, graceSeconds, intervalSeconds, fullscreenDetection, edgeMargin, micro, random);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> checkIdleAndMove(robot, screenSize), 0, intervalSeconds, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            executor.shutdownNow();
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ignored) {
            }
            logger.info("Mouse mover stopped.");
        }));

        logger.info("Mouse mover started. Waiting for idle state...");
        logger.info("Press CTRL+C to stop.");

        while (running) {
            Thread.sleep(500); // keep main thread alive
        }

        return 0;
    }

    private void checkIdleAndMove(Robot robot, Dimension screenSize) {
        Point currentMousePosition = MouseInfo.getPointerInfo().getLocation();
        if (!currentMousePosition.equals(lastMousePosition)) {
            // notify activity to the provider-backed detector
            detector.notifyActivity();
            lastMousePosition = currentMousePosition;
            if (verbose) {
                logger.fine("Mouse moved globally, resetting idle timer.");
            }
        }

        IdleDetector.IdleDecision decision = detector.evaluate(lastMovementAttempt, lastMousePosition, screenSize);

        if (decision.shouldMove) {
            if (verbose) {
                logger.fine("IdleDetector decision: move (reason=" + decision.reason + ")");
            }
            moveMouseHumanLike(robot, screenSize);
            lastMovementAttempt = java.time.Instant.now();
            if (detector != null) detector.notifyActivity();
            lastMousePosition = MouseInfo.getPointerInfo().getLocation();
        } else {
            if (verbose) {
                logger.fine("IdleDetector decision: skip (reason=" + decision.reason + ")");
            }
        }
    }

    private void registerNativeHook() throws NativeHookException {
        Logger nativeLogger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        nativeLogger.setLevel(Level.SEVERE);
        nativeLogger.setUseParentHandlers(false);

        GlobalScreen.registerNativeHook();

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            public void nativeKeyPressed(NativeKeyEvent e) {
                detectedActivity();
            }

            public void nativeKeyReleased(NativeKeyEvent e) {
                detectedActivity();
            }

            public void nativeKeyTyped(NativeKeyEvent e) {
                detectedActivity();
            }
        });

        GlobalScreen.addNativeMouseListener(new NativeMouseInputListener() {
            public void nativeMouseClicked(NativeMouseEvent e) {
                detectedActivity();
            }

            public void nativeMousePressed(NativeMouseEvent e) {
                detectedActivity();
            }

            public void nativeMouseReleased(NativeMouseEvent e) {
                detectedActivity();
            }

            public void nativeMouseMoved(NativeMouseEvent e) {
                detectedActivity();
            }

            public void nativeMouseDragged(NativeMouseEvent e) {
                detectedActivity();
            }
        });
    }

    private void detectedActivity() {
        if (verbose) {
            logger.fine("Mouse/keyboard activity detected globally, resetting idle timer.");
        }
        if (detector != null) detector.notifyActivity();
    }

    private void moveMouseHumanLike(Robot robot, Dimension screenSize) {
        if (micro) {
            // subtle micro-jitter
            int dx = random.nextInt(5) - 2;
            int dy = random.nextInt(5) - 2;
            int nx = Math.max(0, Math.min(screenSize.width - 1, lastMousePosition.x + dx));
            int ny = Math.max(0, Math.min(screenSize.height - 1, lastMousePosition.y + dy));
            if (verbose) {
                logger.fine(String.format("Micro-jitter moving mouse from (%d,%d) to (%d,%d)", lastMousePosition.x, lastMousePosition.y, nx, ny));
            }
            robot.mouseMove(nx, ny);
            try {
                Thread.sleep(10 + random.nextInt(40));
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
            logger.fine(String.format("Moving mouse from (%d,%d) to (%d,%d) in %d steps", startX, startY, targetX, targetY, steps));
        }

        for (int i = 0; i < steps; i++) {
            startX += dx + (random.nextInt(2 * jitter + 1) - jitter);
            startY += dy + (random.nextInt(2 * jitter + 1) - jitter);
            robot.mouseMove(startX, startY);
            try {
                Thread.sleep(10 + random.nextInt(30));
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static void configureLogging(boolean verbose) {
        Logger rootLogger = Logger.getLogger("");
        for (var handler : rootLogger.getHandlers()) rootLogger.removeHandler(handler);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(verbose ? Level.FINE : Level.INFO);

        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(verbose ? Level.FINE : Level.INFO);
    }
}
