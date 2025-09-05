# 🖱️ Mouse Mover

A small Java utility that simulates **human-like random mouse movements** when the computer is idle.  
Useful for keeping sessions active without robotic or predictable movements.

---

## ✨ Features
- Moves mouse in **smooth, random paths** (not robotic).
- Only triggers when the system is **idle** (no keyboard/mouse activity).
- **Configurable** idle threshold and check interval.
- Optional **verbose logging** for debugging.
- Logs both to console and to a `mouse_mover.log` file.
- Cross-platform (runs anywhere with Java and AWT support).

---

## ⚙️ Installation

1. Go to the [Releases page](https://github.com/cptimario/mouse-mover/releases).
2. Download the latest JAR:  
   ```
   mouse-mover-1.0.0.jar
   ```
3. Run with Java:
   ```bash
   java -jar mouse-mover-1.0.0.jar
   ```

---

## 🔧 Configuration

You can pass options as **keyed arguments**:

| Option       | Default | Description |
|--------------|---------|-------------|
| `--idle=SECONDS` | `30`    | Idle threshold before moving the mouse |
| `--interval=SECONDS` | `10`    | How often to check for idleness |
| `--verbose=true` | `false` | Enable verbose logs (FINE level) |

### Examples

Start with defaults:
```bash
java -jar mouse-mover-1.0.0.jar
```

Idle after 30s, check every 10s:
```bash
java -jar mouse-mover-1.0.0.jar --idle=30 --interval=10
```

Enable verbose logging:
```bash
java -jar mouse-mover-1.0.0.jar --verbose
```

---

## 🛑 Stopping
- Press **CTRL+C** in the terminal.
- Or close the hidden window (program runs an invisible frame).

---

## 📦 Releasing (for developers)

If you want to build and release it yourself:

```bash
mvn clean package
mvn release:prepare
mvn release:perform
```

---

## 📜 License
This project is licensed under the [MIT License](LICENSE).

---

## 👨‍💻 Author
**Christopher Timario**  
Built for fun and productivity ✨
