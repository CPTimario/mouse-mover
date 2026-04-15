# 🖱️ Mouse Mover

A lightweight utility that simulates **human-like mouse movements** when your computer is idle—useful for keeping
sessions active without looking robotic or predictable.

---

## ✨ Features

- Moves mouse in **smooth, random paths** (not robotic)
- Triggers only when the system is **idle** (no keyboard/mouse activity)
- **Configurable behavior** (idle threshold, interval, jitter, grace period, edge margin)
- Optional **verbose logging** (DEBUG level) for troubleshooting
- Optional **micro-movements** mode for subtle jitter instead of large moves
- Built-in **fullscreen detection** to avoid moving the mouse during fullscreen apps
- 📦 **Native installers** for macOS, Windows, and Linux (packaged artifacts include native libs)
- 🌍 **Cross-platform support** (native launchers produced so end users don't need to install Java)

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

### Alternatively: Use CI / release-built artifacts (developer or advanced users)

If you prefer a prebuilt installer, packaged JAR, or raw native libraries, the repository's release workflow (tag builds) produces build artifacts you can download from GitHub Actions or the Releases page.

1. Open the repository Actions page and select the latest "Build & Release MouseMover" run (or open the matching GitHub Release for a tagged build).
2. In the selected run's **Artifacts** section (or the Release assets) download the files you need. Artifacts uploaded by the workflow are renamed for clarity and will typically look like:

   - `MouseMover-<version>-macOS.dmg` (macOS installer)
   - `MouseMover-<version>-Windows.exe` (Windows installer)
   - `MouseMover-<version>-Linux.deb` (Debian/Ubuntu package)
   - `MouseMover-<version>-macOS.libidle_time_mac.dylib` (raw macOS native library)
   - `MouseMover-<version>-Windows.idle_time_win.dll` (raw Windows native library)
   - `*.sha256` and optionally `*.sha256.sig` (SHA-256 checksums and signatures)

3. Run a packaged installer for your OS as usual. If you downloaded a JAR or the shaded JAR directly (for example from CI debug output or by building locally), run it with:

```bash
java -jar mouse-mover-<version>-shaded.jar
```

4. If you downloaded only platform-native libraries (e.g. `libidle_time_mac.dylib` or `idle_time_win.dll`) and want to test them locally without the installer, place the binaries under `src/main/resources/native/` so they are available at runtime as `/native/<filename>` inside the application JAR, then build/run the app from Maven:

```bash
# macOS example
cp libidle_time_mac.dylib src/main/resources/native/
mvn clean package
java -jar target/mouse-mover-<version>.jar --verbose
```

Security: the release workflow publishes SHA-256 checksums (and optionally GPG signatures). When present, the application includes a `NativeLoader` that will verify an embedded `native/CHECKSUMS.txt` (SHA-256) before extracting and loading any native library from `/native/` at runtime. Verify checksums locally with e.g.:

```bash
# macOS / Linux
shasum -a 256 libidle_time_mac.dylib
# or
sha256sum libidle_time_mac.dylib
```

If a `.sha256.sig` (GPG signature) is provided, verify it using your GPG setup.

Notes about CI and builds:
- The release workflow uses a matrix build to produce installers on macOS, Windows and Linux and also builds the small native libraries (see `native-src/`).
- The workflow uses the shaded JAR as the application input to `jpackage` for creating installers; raw native libraries and checksum files are uploaded as artifacts for convenience and debugging.
- If you prefer to avoid downloads, you can build the native libraries locally using the scripts under `native-src/mac` and `native-src/win` (they require a JDK and native toolchain). After building, copy the produced `.dylib` / `.dll` into `src/main/resources/native/` and rebuild the Java project.

The application will fall back to a pure‑Java idle time provider if a native library is not present or fails to load; native libraries are optional but provide more accurate OS-level idle detection.

---

## 🚀 Usage

### Default behavior

The app runs with sensible defaults. Important defaults in the current codebase are:

```bash
--idle 30 --interval 5 --jitter 1 --grace 5 --edge-margin 50
```

---

### CLI Options

| Option                       | Default     | Description                                                   |
|-----------------------------:|:-----------:|:-------------------------------------------------------------|
| `--idle=SECONDS`             | `30`        | Idle threshold before moving the mouse (seconds)              |
| `--interval=SECONDS`         | `5`         | How often to check for idleness (seconds)                    |
| `--jitter=PIXELS`            | `1`         | Maximum random jitter per movement step                      |
| `--verbose`                  | `false`     | Enable verbose logs (DEBUG level)                            |
| `--grace=SECONDS`            | `5`         | Extra randomized grace period added after activity (seconds) |
| `--edge-margin=PIXELS`       | `50`        | Pixel margin from screen edge to suppress movement            |
| `--fullscreen-detection`     | `false`     | Avoid moving mouse when a fullscreen app is detected         |
| `--micro`                    | `false`     | Use subtle micro-movements instead of full random moves      |

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

Enable micro-movements and increase the grace period:

```bash
MouseMover --micro --grace 10
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
