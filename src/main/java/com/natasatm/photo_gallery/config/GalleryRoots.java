package com.natasatm.photo_gallery.config;

import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

/**
 * @author Natasa Todorov Markovic
 */

public record GalleryRoots(Path previewRoot, Path originalRoot, Path ordersRoot, Mode mode) {
    public enum Mode { SINGLE, MULTI }
}