package io.github.cptimario.mousemover;

/**
 * Factory to create the best available IdleTimeProvider for the platform, falling back to the JVM provider.
 */
public final class IdleTimeProviderFactory {
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
            // ignore and fallback
        }
        return fallback;
    }
}

