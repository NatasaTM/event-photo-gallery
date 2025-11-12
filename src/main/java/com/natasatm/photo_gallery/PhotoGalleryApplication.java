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

        Path appHome = AppData.resolveDefaultDataDir();
        try {
            Files.createDirectories(appHome);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // više nije baza: generalni app-home
        System.setProperty("APP_HOME", appHome.toString().replace("\\", "/"));


        SpringApplication.run(PhotoGalleryApplication.class, args);
    }
}
