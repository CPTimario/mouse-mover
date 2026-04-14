# 🖱️ Mouse Mover

A lightweight utility that simulates **human-like mouse movements** when your computer is idle—useful for keeping
sessions active without looking robotic or predictable.

---

## ✨ Features

* Moves mouse in **smooth, random paths** (not robotic)
* Triggers only when the system is **idle** (no keyboard/mouse activity)
* **Configurable behavior** (idle threshold, interval, jitter)
* Optional **verbose logging** for debugging
* 📦 **Native installers** for macOS, Windows, and Linux
* 🌍 **Cross-platform support** (no Java installation required for end users)

---

## 📦 Installation

1. Go to the [Releases page](https://github.com/cptimario/mouse-mover/releases)
2. Download the appropriate installer for your OS:

| OS      | File Type |
|---------|-----------|
| macOS   | `.dmg`    |
| Windows | `.exe`    |
| Linux   | `.deb`    |

3. Install and run the application

### Alternatively: Use CI-built artifacts (developer or advanced users)

If you prefer the lightweight packaged JAR or need native libraries directly, the GitHub Actions workflow `build-native` produces artifacts for each run:

1. Open the repository Actions page and select the latest `build-native` run.
2. In the selected run, open the **Artifacts** section and download one of the following:

   - `mouse-mover-package` — a packaged JAR that includes native libraries for macOS and Windows (embedded under `native/` resources)
   - `libidle_time_mac.dylib` — macOS native library
   - `idle_time_win.dll` — Windows native library

3. If you downloaded the packaged JAR, run it directly with:

```bash
java -jar mouse-mover-<version>-shaded.jar
```

4. If you downloaded platform-native libraries and want to test them locally, place the binaries under `src/main/resources/native/` (for quick local testing) and run the application from Maven, or use the packaged JAR approach above.

Security: the workflow also publishes SHA-256 checksums and optional GPG signatures for native artifacts. When present, the packaged JAR contains a `native/CHECKSUMS.txt` file that `NativeLoader` verifies at runtime before loading any embedded native library.

---

## 🚀 Usage

### Default behavior

The app runs with sensible defaults:

```bash
--idle 30 --interval 5
```

---

### CLI Options

| Option               | Default | Description                             |
|----------------------|---------|-----------------------------------------|
| `--idle=SECONDS`     | `30`    | Idle threshold before moving the mouse  |
| `--interval=SECONDS` | `5`     | How often to check for idleness         |
| `--jitter=PIXELS`    | `1`     | Maximum random jitter per movement step |
| `--verbose`          | `false` | Enable verbose logs (FINE level)        |

---

### Examples

Run with defaults:

```bash
MouseMover
```

Idle after 30s, check every 10s:

```bash
MouseMover --idle 30 --interval 10
```

Enable verbose logging and increase jitter:

```bash
MouseMover --verbose --jitter 3
```

---

## ⚠️ Important Notes

### macOS Permissions

This app requires **Accessibility permissions** to detect input and move the mouse:

```
System Settings → Privacy & Security → Accessibility
```

Without this, the app will not function correctly.

---

### Windows SmartScreen

Windows may show a warning for unsigned applications.
This is expected for newly built tools.

---

### Linux

`.deb` packages are supported (Debian/Ubuntu-based systems).

---

## 🛑 Stopping the App

* Press **CTRL + C** in the terminal
* Or close the application process

---

The CLI is designed to run continuously: when you start the app it creates a background scheduled task that periodically
checks for idleness and moves the mouse. The process intentionally blocks (the main thread calls `join()` on the
service) and will keep running until the service is stopped. Use CTRL+C (or send SIGTERM) to trigger the JVM shutdown
hook which calls `stop()` and allows the process to exit cleanly.

If you are writing automated tests or need a single-run invocation for debugging, prefer the test-friendly entrypoints
or patterns described below.

## 🧱 Development

### Build locally

```bash
mvn clean package
```

---

### Create release (automated)

Releases are built automatically via GitHub Actions:

```bash
git tag v1.2.0
git push origin v1.2.0
```

This will:

* Build the application
* Generate installers for all platforms
* Publish a GitHub Release with downloadable assets

---

## 🛠 Tech Stack

* Java
* JNativeHook (global input detection)
* AWT Robot (mouse control)
* Picocli (CLI parsing)

---

## 🧪 Testing and non-blocking runs

When running under a test harness you should avoid starting the real, blocking service. The project exposes a couple of
test-friendly hooks:

- `Launcher.executeWithReturn(String[] args)` — invokes the CLI and returns the exit code without calling
  `System.exit(...)`, useful for unit tests that want to exercise CLI parsing.
- `Launcher#createService()` — a package-private factory method used by `Launcher` to instantiate the
  `MouseMoverService`. Tests can subclass `Launcher` and override this to return a no-op or deterministic service so
  `call()` returns immediately (see `src/test/java/.../LauncherTest.java`).
- `MouseMoverService.startWithRobot(MouseRobot, Dimension)` — a package-private test-friendly start method that accepts
  a `MouseRobot` implementation (the `TestMouseRobot` under `src/test/java/.../testutil/` records mouse moves and no-ops
  sleeps). Tests can start the service, allow the scheduled task to run, then call `stop()` and `join()` to ensure
  deterministic cleanup (see `MouseMoverServiceStartTest`).

Run the full test suite with Maven:

```bash
mvn -DskipTests=false test
```

These patterns ensure tests don't hang the build by starting a long-running CLI instance.

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

## 👨‍💻 Author

**Christopher Timario**
Built for fun and productivity ✨
