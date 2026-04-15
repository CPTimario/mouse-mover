package io.github.cptimario.mousemover.platform.nativeimpl;

import io.github.cptimario.mousemover.platform.IdleTimeProvider;

/**
 * JNI-backed macOS idle time provider. Expects libidle_time_mac.dylib to be available as a resource
 * under /native/ or on java.library.path.
 */
public class MacOSIdleTimeProvider implements IdleTimeProvider {
  static {
    NativeLoader.load("libidle_time_mac.dylib");
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
