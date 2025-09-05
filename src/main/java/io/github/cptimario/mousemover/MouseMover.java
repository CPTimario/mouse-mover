package io.github.cptimario.mousemover;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.*;

import javax.swing.*;

public class MouseMover implements Callable<Integer> {
    private static final Logger logger = Logger.getLogger(MouseMover.class.getName());
    private static final Random random = new Random();
    private static volatile boolean running = true;

    @Option(names = "--idle", description = "Idle threshold before moving the mouse (seconds)", defaultValue = "30")
    int idleSeconds;

    @Option(names = "--interval", description = "How often to check for idleness (seconds)", defaultValue = "10")
    int intervalSeconds;

    @Option(names = "--verbose", description = "Enable verbose logging (FINE level)")
    boolean verbose;

    private Instant lastActivity = Instant.now();

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MouseMover()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        configureLogging(verbose);

        Robot robot = new Robot();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        // Hidden frame to allow AWT event listening
        JFrame frame = new JFrame("MouseMover");
        frame.setUndecorated(true);
        frame.setOpacity(0f);
        frame.setAlwaysOnTop(true);
        frame.setSize(1, 1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Capture keyboard & mouse events
        toolkit.addAWTEventListener(e -> {
            lastActivity = Instant.now();
            if (verbose) {
                logger.fine("User activity detected via AWT event, resetting idle timer.");
            }
        }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Mouse mover stopped.");
            running = false;
        }));

        frame.setVisible(false);

        logger.info("Mouse mover started. Waiting for idle state...");
        logger.info("Press CTRL+C to stop.");

        Point lastLocation = MouseInfo.getPointerInfo().getLocation();

        try {
            while (running) {
                // Detect global mouse movement
                Point current = MouseInfo.getPointerInfo().getLocation();
                if (!current.equals(lastLocation)) {
                    lastActivity = Instant.now();
                    lastLocation = current;
                    if (verbose) {
                        logger.fine("Mouse movement detected globally, resetting idle timer.");
                    }
                }

                // Check idle time
                if (Duration.between(lastActivity, Instant.now()).toSeconds() >= idleSeconds) {
                    moveMouseHumanLike(robot, screenSize);
                    lastActivity = Instant.now();
                }

                Thread.sleep(intervalSeconds * 1000L);
            }
        } catch (InterruptedException e) {
            logger.info("Mouse mover interrupted.");
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
        }

        return 0;
    }

    private static void moveMouseHumanLike(Robot robot, Dimension screenSize) {
        int targetX = random.nextInt(screenSize.width);
        int targetY = random.nextInt(screenSize.height);

        int startX = MouseInfo.getPointerInfo().getLocation().x;
        int startY = MouseInfo.getPointerInfo().getLocation().y;

        int steps = 30 + random.nextInt(40);
        int dx = (targetX - startX) / steps;
        int dy = (targetY - startY) / steps;

        for (int i = 0; i < steps; i++) {
            startX += dx + (random.nextInt(3) - 1); // small jitter
            startY += dy + (random.nextInt(3) - 1);
            robot.mouseMove(startX, startY);
            try {
                Thread.sleep(10 + random.nextInt(30));
            } catch (InterruptedException ignored) {}
        }

        logger.fine(String.format("Moved mouse to (%d, %d)", targetX, targetY));
    }

    private static void configureLogging(boolean verbose) {
        Logger rootLogger = Logger.getLogger("");
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(verbose ? Level.FINE : Level.INFO);

        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(verbose ? Level.FINE : Level.INFO);
    }
}
