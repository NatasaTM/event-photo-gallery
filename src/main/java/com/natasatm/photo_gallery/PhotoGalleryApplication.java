package com.natasatm.photo_gallery;

import com.natasatm.photo_gallery.data.AppData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class PhotoGalleryApplication {

    public static void main(String[] args) {
        // MORA biti pre SpringApplication.run()!
        System.setProperty("java.awt.headless", "false");

        Path dataDir = AppData.resolveDefaultDataDir();
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.setProperty("APP_DATA_DIR", dataDir.toString().replace("\\", "/")); // koristi se u properties

        SpringApplication.run(PhotoGalleryApplication.class, args);
    }
}
