package io.github.cptimario.mousemover;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

            // Read bytes and compute sha256
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
                baos.write(buffer, 0, read);
            }
            byte[] bytes = baos.toByteArray();
            String shaHex = bytesToHex(md.digest());

            // If CHECKSUMS.txt is embedded, load and verify
            Map<String, String> checksums = loadChecksums();
            if (checksums.containsKey(libName)) {
                String expected = checksums.get(libName);
                if (!expected.equalsIgnoreCase(shaHex)) {
                    throw new RuntimeException("Checksum mismatch for " + libName + ": expected=" + expected + " computed=" + shaHex);
                }
            }

            Path temp = Files.createTempFile(libName, null);
            Files.write(temp, bytes);
            System.load(temp.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library " + libName, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify/load native library " + libName, e);
        }
    }

    private static Map<String, String> loadChecksums() {
        Map<String, String> map = new HashMap<>();
        try (InputStream is = NativeLoader.class.getResourceAsStream("/native/CHECKSUMS.txt")) {
            if (is == null) return map;
            try (Scanner s = new Scanner(is, StandardCharsets.UTF_8)) {
                while (s.hasNextLine()) {
                    String line = s.nextLine().trim();
                    if (line.isEmpty()) continue;
                    // expected format: "<hash>  filename"
                    String[] parts = line.split("\\s+", 2);
                    if (parts.length == 2) {
                        map.put(parts[1].trim(), parts[0].trim());
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return map;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}

