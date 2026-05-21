package io.github.cptimario.mousemover.platform;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pure-Java fallback implementation. Tracks activity via explicit markActivity() calls (e.g. mouse
 * polling) and via a global keyboard hook registered through JNativeHook so typing alone resets the
 * idle timer.
 */
public class JvmIdleTimeProvider implements IdleTimeProvider {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JvmIdleTimeProvider.class);

  // Strong reference required — JUL loggers are weakly referenced and configuration is lost if
  // the logger is GC'd (SpotBugs LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE).
  private static final Logger JNATIVEHOOK_JUL_LOGGER =
      Logger.getLogger(GlobalScreen.class.getPackage().getName());

  static {
    JNATIVEHOOK_JUL_LOGGER.setLevel(Level.WARNING);
    JNATIVEHOOK_JUL_LOGGER.setUseParentHandlers(false);
  }

  private volatile Instant lastActivity = Instant.now();
  private final NativeKeyListener keyListener;
  private final boolean hookRegistered;

  public JvmIdleTimeProvider() {
    this.keyListener =
        new NativeKeyListener() {
          @Override
          public void nativeKeyPressed(NativeKeyEvent e) {
            markActivity();
          }
        };
    this.hookRegistered = registerHook(keyListener);
  }

  @Override
  public long getIdleTimeSeconds() {
    return Duration.between(lastActivity, Instant.now()).toSeconds();
  }

  @Override
  public void markActivity() {
    lastActivity = Instant.now();
  }

  @Override
  public void close() {
    if (!hookRegistered) {
      return;
    }
    try {
      GlobalScreen.removeNativeKeyListener(keyListener);
      GlobalScreen.unregisterNativeHook();
    } catch (NativeHookException e) {
      logger.debug("Failed to unregister JNativeHook: {}", e.getMessage(), e);
    }
  }

  /** Visible for testing. */
  boolean isHookRegistered() {
    return hookRegistered;
  }

  /** Visible for testing — exposes the listener so tests can fire synthetic key events. */
  NativeKeyListener keyListener() {
    return keyListener;
  }

  private static boolean registerHook(NativeKeyListener listener) {
    try {
      if (!GlobalScreen.isNativeHookRegistered()) {
        GlobalScreen.registerNativeHook();
      }
      GlobalScreen.addNativeKeyListener(listener);
      return true;
    } catch (NativeHookException e) {
      logger.warn(
          "Could not register global keyboard hook; keyboard activity will not reset idle timer. "
              + "Reason: {}",
          e.getMessage());
      return false;
    } catch (Throwable t) {
      logger.warn(
          "Unexpected error registering global keyboard hook; keyboard activity will not reset "
              + "idle timer. Reason: {}",
          t.getMessage());
      return false;
    }
  }
}
