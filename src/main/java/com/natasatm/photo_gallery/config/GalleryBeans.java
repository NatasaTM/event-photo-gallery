package com.natasatm.photo_gallery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Natasa Todorov Markovic
 */
@Configuration
public class GalleryBeans {

    @Bean
    public Path galleryRoot(@Value("${gallery.folder:./gallery}") String folder) {
        return Paths.get(System.getenv().getOrDefault("GALLERY_FOLDER", folder));
    }
}
