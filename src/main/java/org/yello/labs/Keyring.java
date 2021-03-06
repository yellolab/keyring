package org.yello.labs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Keyring {
  static {
    String nativeLibraryName;
    { // Determine the operating system, to determine which library to expand into the host system
      String OS = System.getProperty("os.name").toLowerCase();
      boolean IS_WINDOWS = (OS.contains("win"));
      boolean IS_MAC = (OS.contains("mac"));
      boolean IS_UNIX = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));

      if (IS_WINDOWS) {
        nativeLibraryName = "keyring_jni.dll"; // why do you do this to me cargo?
      } else if (IS_MAC) {
        nativeLibraryName = "libkeyring_jni.dylib";
      } else if (IS_UNIX) {
        nativeLibraryName = "libkeyring_jni.so";
      } else {
        throw new RuntimeException("The OS {" + OS + "} is not supported by Keyring currently");
      }
    }

    // Find the library in the java resources, and expand into a temp directory
    // We assume that the system that compiled this either had ALL libraries, or at least the one
    // that is needed for local running
    File temp;
    try (InputStream in = Keyring.class.getClassLoader().getResourceAsStream(nativeLibraryName)) {
      if (Objects.isNull(in)) {
        throw new RuntimeException(
            "The library {" + nativeLibraryName + "} was not found in the Keyring library");
      }

      byte[] buffer = new byte[1024];
      int read = -1;
      temp = File.createTempFile(nativeLibraryName, "");
      try (FileOutputStream fos = new FileOutputStream(temp)) {

        while ((read = in.read(buffer)) != -1) {
          fos.write(buffer, 0, read);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not generate DLL for the Keyring class", e);
    }
    System.load(temp.getAbsolutePath());
  }

  public static native String getSecret(String domain, String userName) throws Exception;

  public static native String setSecret(String domain, String username, String secret)
      throws Exception;

  public static native String deleteSecret(String domaind, String username) throws Exception;
}
