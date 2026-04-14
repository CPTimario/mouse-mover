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

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

## 👨‍💻 Author

**Christopher Timario**
Built for fun and productivity ✨
