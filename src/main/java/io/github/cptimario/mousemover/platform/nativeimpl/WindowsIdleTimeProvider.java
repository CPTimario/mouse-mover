package io.github.cptimario.mousemover.platform.nativeimpl;

import io.github.cptimario.mousemover.platform.IdleTimeProvider;

/**
 * JNI-backed Windows idle time provider. Expects idle_time_win.dll available under /native/ or on
 * library path.
 */
public class WindowsIdleTimeProvider implements IdleTimeProvider {
  static {
    try {
      NativeLoader.load("idle_time_win.dll");
    } catch (Throwable t) {
      // allow fallback
    }
  }

  private native long getIdleTimeMillisNative();

  @Override
  public long getIdleTimeSeconds() {
    try {
      return getIdleTimeMillisNative() / 1000L;
    } catch (Throwable t) {
      return 0L;
    }
  }
}
