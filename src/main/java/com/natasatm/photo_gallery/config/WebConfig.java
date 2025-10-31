package com.natasatm.photo_gallery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @author Natasa Todorov Markovic
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${gallery.folder:./gallery}")
    private String galleryFolderProp;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String folder = System.getenv().getOrDefault("GALLERY_FOLDER", galleryFolderProp);
        Path root = Paths.get(folder).toAbsolutePath().normalize();

        // Convert Windows backslashes to forward slashes for file: URLs
        String location = "file:" + root.toString().replace("\\", "/") + "/";

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location) // ← obavezno / na kraju
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
    }
}