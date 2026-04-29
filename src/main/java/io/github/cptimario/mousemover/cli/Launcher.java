package io.github.cptimario.mousemover.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.github.cptimario.mousemover.core.MouseMoverService;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/** CLI launcher that parses arguments and starts the MouseMoverService. */
public class Launcher implements Callable<Integer> {
  @Option(
      names = "--idle",
      description = "Idle threshold before moving the mouse (seconds)",
      defaultValue = "30")
  int idleSeconds;

  @Option(
      names = "--interval",
      description = "How often to check for idleness (seconds)",
      defaultValue = "5")
  int intervalSeconds;

  @Option(names = "--verbose", description = "Enable verbose logging (DEBUG level)")
  boolean verbose;

  @Option(names = "--jitter", description = "Max pixel jitter per step", defaultValue = "1")
  int jitter;

  @Option(
      names = "--grace",
      description = "Extra grace period after activity (seconds)",
      defaultValue = "5")
  int graceSeconds;

  @Option(
      names = "--fullscreen-detection",
      description = "Avoid moving mouse when fullscreen app is active")
  boolean fullscreenDetection;

  @Option(names = "--micro", description = "Use subtle micro-movements instead of large moves")
  boolean micro;

  @Option(
      names = "--edge-margin",
      description = "Pixel margin from screen edge to suppress movement",
      defaultValue = "50")
  int edgeMargin;

  static void main(String[] args) {
    System.exit(executeWithReturn(args));
  }

  @Override
  public Integer call() {
    // If tests set `testMode` we should not start the long-running service so
    // the test-friendly entrypoint (`executeForTests`) can execute CLI parsing
    // without hanging the JVM.
    if (testMode) {
      return 0;
    }

    // If the user requested verbose output, programmatically lower the root
    // logging level to DEBUG so debug statements (logger.debug) are emitted.
    try {
      if (verbose) {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
      }
    } catch (Throwable ignored) {
      // If logback is not available or changing level fails, continue without
      // crashing; verbose will still control internal verbose flags.
    }

    final MouseMoverService service = createService();
    service.start();

    Runtime.getRuntime().addShutdownHook(new Thread(service::stop));

    service.join();
    return 0;
  }

  /** Package-private hook to create the service; tests may override. */
  MouseMoverService createService() {
    return new MouseMoverService(
        idleSeconds,
        intervalSeconds,
        graceSeconds,
        jitter,
        edgeMargin,
        fullscreenDetection,
        micro,
        verbose);
  }

  /** Test-friendly entry that returns the CLI exit code without terminating the JVM. */
  static int executeWithReturn(String[] args) {
    return new CommandLine(new Launcher()).execute(args);
  }

  /**
   * Test-friendly entrypoint that executes CLI parsing but does not start the long-running service.
   * Tests may call this to avoid hanging the JVM; it returns the CLI exit code.
   */
  static int executeForTests(String[] args) {
    Launcher l = new Launcher();
    l.testMode = true;
    return new CommandLine(l).execute(args);
  }

  // package-private flag used by the test-friendly entrypoint to avoid starting the
  // long-running service. Default is false for normal CLI runs.
  boolean testMode = false;
}
