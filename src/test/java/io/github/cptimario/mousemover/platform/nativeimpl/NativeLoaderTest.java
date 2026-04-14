package io.github.cptimario.mousemover.platform.nativeimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class NativeLoaderTest {

  @Test
  public void testBytesToHex() {
    byte[] b = new byte[] {0x0f, (byte) 0xa0};
    String hex = NativeLoader.bytesToHex(b);
    assertEquals("0fa0", hex);
  }

  @Test
  public void testLoadChecksums_parsesFile() {
    Map<String, String> map = NativeLoader.loadChecksums();
    // Test resource contains a mapping for libfake.dylib (sha256 of 'hello')
    assertEquals(
        "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
        map.get("libfake.dylib"));
  }

  @Test
  public void testLoad_withMatchingChecksum_attemptsSystemLoad() {
    // resource libfake.dylib is present in test resources and CHECKSUMS contains matching sha.
    // System.load will fail for a non-native file; ensure the call throws a RuntimeException.
    assertThrows(RuntimeException.class, () -> NativeLoader.load("libfake.dylib"));
  }

  @Test
  public void testLoad_missingResourceThrows() {
    assertThrows(RuntimeException.class, () -> NativeLoader.load("no-such-file.dylib"));
  }
}
