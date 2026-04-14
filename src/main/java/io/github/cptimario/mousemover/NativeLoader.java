package io.github.cptimario.mousemover;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Helper to load native libraries packaged as resources under /native/.
 */
public final class NativeLoader {
    private NativeLoader() {}

    public static void load(String libName) {
        String resourcePath = "/native/" + libName;
        try (InputStream is = NativeLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Native resource not found: " + resourcePath);
            }
            Path temp = Files.createTempFile(libName, null);
            Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
            System.load(temp.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library " + libName, e);
        }
    }
}

