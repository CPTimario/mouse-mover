package io.github.cptimario.mousemover.cli;

import io.github.cptimario.mousemover.core.MouseMoverService;
import java.util.concurrent.Callable;
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
  boolean fullscreenDetection = true;

  @Option(names = "--micro", description = "Use subtle micro-movements instead of large moves")
  boolean micro;

  @Option(
      names = "--edge-margin",
      description = "Pixel margin from screen edge to suppress movement",
      defaultValue = "50")
  int edgeMargin;

  static void main(String[] args) {
    int exitCode = new CommandLine(new Launcher()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    final MouseMoverService service =
        new MouseMoverService(
            idleSeconds,
            intervalSeconds,
            graceSeconds,
            jitter,
            edgeMargin,
            fullscreenDetection,
            micro,
            verbose);
    service.start();

    Runtime.getRuntime().addShutdownHook(new Thread(service::stop));

    service.join();
    return 0;
  }
}
