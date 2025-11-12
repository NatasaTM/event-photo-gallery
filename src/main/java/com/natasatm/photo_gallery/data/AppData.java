package com.natasatm.photo_gallery.data;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Natasa Todorov Markovic
 */
public final class AppData {
    private AppData() {}

    public static Path resolveDefaultDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String local = System.getenv("LOCALAPPDATA");
            if (local != null && !local.isBlank()) return Paths.get(local, "PhotoGallery");
            return Paths.get(System.getProperty("user.home"), "AppData", "Local", "PhotoGallery");
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "PhotoGallery");
        } else { // Linux
            String xdg = System.getenv("XDG_DATA_HOME");
            if (xdg != null && !xdg.isBlank()) return Paths.get(xdg, "PhotoGallery");
            return Paths.get(System.getProperty("user.home"), ".local", "share", "PhotoGallery");
        }
    }
}
