package com.natasatm.photo_gallery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author Natasa Todorov Markovic
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FolderResolver folderResolver;

    public WebConfig(FolderResolver folderResolver) {
        this.folderResolver = folderResolver;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // ← OVO poziva dijalog/uzima zapamćen/ENV/property – sve kroz FolderResolver
        Path root = folderResolver.getRoot();

        // ResourceLocations moraju biti file: URL sa kosom crtom na kraju
        String location = root.toUri().toString(); // npr. "file:/C:/Users/.../"

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location) // obavezno / na kraju, toUri() ga već daje
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .resourceChain(true);
    }
}