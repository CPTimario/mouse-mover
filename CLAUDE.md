# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Full build (compile, test, static analysis, package)
just build          # or: mvn -B -U verify

# Run tests only
just test           # or: mvn -B test

# Package without tests
just package        # or: mvn -B -U -DskipTests=true package

# Apply code formatting (must pass before commits)
just format         # or: mvn spotless:apply

# Check formatting without applying (CI-style)
just format-check   # or: mvn -B -U spotless:check

# Run full local CI check (format-check + verify)
just ci

# Run the built JAR
just run-jar        # or: java -jar target/mouse-mover-<version>.jar

# Clean build artifacts
just clean
```

Static analysis (`mvn verify`) runs Checkstyle (Google style) and SpotBugs. Both must pass.

## Architecture

The app is a Java CLI that periodically moves the mouse when the system is idle.

**Entry point:** `cli/Launcher.java` — Picocli command that parses CLI args and starts `MouseMoverService`. It blocks on `service.join()` until SIGTERM/Ctrl-C fires the shutdown hook.

**Core service:** `core/MouseMoverService.java` — Schedules a fixed-rate task that calls `checkIdleAndMove()`. Delegates idle decisions to `IdleDetector` and mouse moves to `MouseRobot`.

**Idle detection:** `detector/IdleDetector.java` — Pure logic class (no AWT/Robot) that evaluates whether to move based on idle time, grace period, cooldown, fullscreen state, and edge proximity. Returns an `IdleDecision` record. Designed to be unit-testable in isolation.

**Platform idle time:** `platform/` — `IdleTimeProvider` interface with three implementations:
- `JvmIdleTimeProvider` — JVM-only fallback using JNativeHook
- `MacOSIdleTimeProvider` / `WindowsIdleTimeProvider` — native JNI via `NativeLoader`
- `IdleTimeProviderFactory` selects the best available provider at runtime

**Native libraries:** C sources in `native-src/mac/` and `native-src/win/`. Compiled `.dylib`/`.dll` are NOT committed; CI builds them and packages them into `src/main/resources/native/`. `NativeLoader` verifies checksums from `native/CHECKSUMS.txt` before loading.

**Mouse abstraction:** `core/MouseRobot.java` interface implemented by `AwtMouseRobot` (production) and `testutil/TestMouseRobot` (test — records moves, no-ops sleeps).

## Testing Patterns

The service runs a long-running blocking loop, so tests use specific hooks to avoid hanging:

- `Launcher.executeForTests(args)` — parses CLI without starting the service (`testMode = true`)
- `Launcher.executeWithReturn(args)` — full execution, returns exit code without `System.exit()`
- `Launcher#createService()` — package-private factory; override in subclass to inject a no-op service
- `MouseMoverService.startWithRobot(MouseRobot, Dimension)` — package-private; inject `TestMouseRobot` and a fixed `Dimension` to avoid real AWT
- The test pattern is: `startWithRobot(...)` → allow scheduled task to run → `stop()` + `join()`

JUnit 5 + Mockito. The `byte-buddy-agent` is pre-configured as a `-javaagent` in Surefire to suppress dynamic-attach warnings on newer JDKs.

## Code Style

- Google Java Format via Spotless (`mvn spotless:apply`)
- Checkstyle rules: `config/checkstyle/google_checks.xml`
- Java 25 source/target (`--enable-preview` not used)
- SpotBugs annotations (`@SuppressFBWarnings`) are available for intentional suppressions