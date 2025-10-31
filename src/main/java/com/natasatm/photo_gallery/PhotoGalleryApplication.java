package com.natasatm.photo_gallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PhotoGalleryApplication {

    public static void main(String[] args) {
        // MORA biti pre SpringApplication.run()!
        System.setProperty("java.awt.headless", "false");

        SpringApplication.run(PhotoGalleryApplication.class, args);
    }
}
