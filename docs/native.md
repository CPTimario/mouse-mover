Native libraries
================

This project supports optional OS-native idle-time detection via small JNI libraries.

What is included in the repo
- `native-src/` – C source and simple build scripts for macOS and Windows. These are the canonical
  sources used to build the native libraries.

What is NOT included in the main branch
- Compiled binaries (`.dylib` / `.dll`) are not committed here by default. CI builds should produce
  platform-specific binaries and publish them as release artifacts.

How to build locally

- macOS (clang):

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
cd native-src/mac
./build-mac.sh
```

- Windows (MinGW):

```bash
set JAVA_HOME=C:\Path\To\JDK
cd native-src/win
./build-win.sh
```

How to use
- The Java shims (`MacOSIdleTimeProvider` / `WindowsIdleTimeProvider`) load the native library via
  `NativeLoader` which extracts a resource under `/native/` and calls `System.load(...)`.

Permissions
- On macOS you must grant Accessibility permissions to the built application so it can observe input
  events. See Apple documentation for more details.

