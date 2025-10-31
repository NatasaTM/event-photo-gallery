package com.natasatm.photo_gallery.service;

import com.natasatm.photo_gallery.config.FolderResolver;
import com.natasatm.photo_gallery.model.GalleryItem;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.natasatm.photo_gallery.model.GalleryItem;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author Natasa Todorov Markovic
 */
@Service
public class GalleryService {

    @Getter
    private final Path root;
    // već je sortiran po imenu fajla; po potrebi sortiraj i ključeve
    @Getter
    private volatile Map<String, List<GalleryItem>> index = new HashMap<>();
    private final AtomicLong version = new AtomicLong(0);

    public GalleryService(Path galleryRoot) throws IOException {
        this.root = galleryRoot.toAbsolutePath().normalize();
        Files.createDirectories(this.root);
        rebuildAndBump(); // inicijalno punjenje
    }

    public synchronized void rebuildAndBump() {
        this.index = scan();
        version.incrementAndGet();
    }

    public long getVersion() {
        return version.get();
    }

    // === pomoćno ===
    private Map<String, List<GalleryItem>> scan() {
        Map<String, List<GalleryItem>> result = new HashMap<>();
        if (!Files.exists(root)) return result;

        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(root, Files::isDirectory)) {
            for (Path dir : dirs) {
                List<GalleryItem> items = Files.list(dir)
                        .filter(Files::isRegularFile)
                        .filter(p -> isAllowed(p.getFileName().toString()))
                        .map(p -> {
                            long m = p.toFile().lastModified();
                            String name = p.getFileName().toString();
                            // cache-bust query param
                            String url = "/images/" + dir.getFileName() + "/" + name + "?v=" + m;
                            return new GalleryItem(name, url, m);
                        })
                        .sorted(Comparator.comparing(GalleryItem::name, (a,b) ->
                                a.toLowerCase().compareTo(b.toLowerCase())))
                        .collect(Collectors.toList());
                if (!items.isEmpty()) {
                    result.put(dir.getFileName().toString(), items);
                }
            }
        } catch (IOException ignored) {}
        return result;
    }

    private static final Set<String> ALLOWED = Set.of(".jpg",".jpeg",".png",".gif",".webp",".bmp");
    private boolean isAllowed(String fn) {
        String lower = fn.toLowerCase(Locale.ROOT);
        return ALLOWED.stream().anyMatch(lower::endsWith);
    }





}
