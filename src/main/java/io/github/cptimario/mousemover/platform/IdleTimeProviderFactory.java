package io.github.cptimario.mousemover.platform;

import io.github.cptimario.mousemover.platform.nativeimpl.MacOSIdleTimeProvider;
import io.github.cptimario.mousemover.platform.nativeimpl.WindowsIdleTimeProvider;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create the best available IdleTimeProvider for the platform, falling back to the JVM
 * provider.
 */
public final class IdleTimeProviderFactory {
  private static final Logger logger = LoggerFactory.getLogger(IdleTimeProviderFactory.class);

  private IdleTimeProviderFactory() {}

  public static IdleTimeProvider create(JvmIdleTimeProvider fallback) {
    String os = System.getProperty("os.name", "").toLowerCase();
    try {
      if (os.contains("mac")) {
        return new MacOSIdleTimeProvider();
      } else if (os.contains("win")) {
        return new WindowsIdleTimeProvider();
      }
    } catch (Throwable t) {
      // Prefer the top-level message, but if that's null prefer the cause message
      String reason = Optional.ofNullable(t.getMessage()).orElse(t.getCause().getMessage());
      // Log a short, human-readable warning at WARN level and include the full
      // throwable only at DEBUG level to avoid noisy stacktraces in normal logs.
      logger.warn(
          "Native IdleTimeProvider unavailable for os='{}'. Falling back to JVM provider. Reason: {}",
          os,
          reason);
      if (logger.isDebugEnabled()) {
        logger.debug("Native IdleTimeProvider instantiation failure details:", t);
      }
    }
    return fallback;
  }
}
