package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.config.FolderResolver;
import com.natasatm.photo_gallery.model.GalleryItem;
import com.natasatm.photo_gallery.service.GalleryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Natasa Todorov Markovic
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final GalleryService gallery;
    private final FolderResolver folderResolver;
    public ApiController(GalleryService gallery, FolderResolver folderResolver) { this.gallery = gallery;
        this.folderResolver = folderResolver;
    }

    @GetMapping("/gallery")
    public Map<String, List<GalleryItem>> gallery() {
        return gallery.getIndex();
    }

    @GetMapping("/debug")
    public Map<String, Object> debug() {
        return Map.of(
                "folders", gallery.getIndex().size(),
                "total_images", gallery.getIndex().values().stream().mapToInt(List::size).sum(),
                "gallery_folder", gallery.getRoot().toString(),
                "gallery_folder_resolver", folderResolver.getRoot().toString(), // ← Proveri šta resolver vidi
                "version", gallery.getVersion()
        );
    }
}
