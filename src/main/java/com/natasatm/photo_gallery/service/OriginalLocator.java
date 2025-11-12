package com.natasatm.photo_gallery.service;

import org.springframework.stereotype.Service;
import com.natasatm.photo_gallery.config.GalleryRoots;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Natasa Todorov Markovic
 */
@Service
public class OriginalLocator {

    private final Path originalsRoot;

    // filename(lower) -> sve relativne putanje gde se taj fajl nalazi
    private final Map<String, List<String>> nameIndex = new ConcurrentHashMap<>();

    // Dozvoljene ekstenzije (ako zatreba više od jpg)
    private static final Set<String> ALLOWED = Set.of("jpg","jpeg","png");

    public OriginalLocator(GalleryRoots roots) {
        this.originalsRoot = roots.originalRoot().toAbsolutePath().normalize();
        try { rebuildIndex(); } catch (Exception ignored) {}
    }

    /** Glavni ulaz: iz previewRelPath (npr. "A/DSC_0001.jpg") vrati originalRelPath pod originals/. */
    public Optional<String> findOriginalByPreviewRel(String previewRelPath) {
        String rel = normalizeRel(previewRelPath);
        String file = fileName(rel).toLowerCase(Locale.ROOT);
        if (!isAllowed(file)) return Optional.empty();

        // Kandidati po imenu fajla
        List<String> candidates = nameIndex.getOrDefault(file, List.of());
        if (candidates.isEmpty()) {
            // Prvi put ili posle većeg kopiranja možda indeks nije sve uhvatio — probaj fallback skeniranje
            Optional<String> scanned = scanOnceByFilename(file);
            scanned.ifPresent(path -> nameIndex.put(file, new ArrayList<>(List.of(path))));
            return scanned;
        }
        if (candidates.size() == 1) return Optional.of(candidates.get(0));

        // Heuristika: probaj isti parent folder kao u preview-u (npr. "A")
        String previewParent = parentDir(rel);
        for (String c : candidates) {
            if (Objects.equals(previewParent, parentDir(c))) return Optional.of(c);
        }

        // Ako i dalje ima više, uzmi prvi (ili vrati empty ako želiš ručno razrešavanje)
        return Optional.of(candidates.get(0));
    }

    /** Pozovi kad misliš da se originals promenio (ili periodično). */
    public synchronized void rebuildIndex() throws IOException {
        Map<String, List<String>> idx = new HashMap<>();
        try (var walk = Files.walk(originalsRoot)) {
            walk.filter(Files::isRegularFile).forEach(p -> {
                String rel = originalsRoot.relativize(p.toAbsolutePath().normalize()).toString().replace('\\','/');
                String fn  = fileName(rel).toLowerCase(Locale.ROOT);
                if (isAllowed(fn)) {
                    idx.computeIfAbsent(fn, k -> new ArrayList<>()).add(rel);
                }
            });
        }
        idx.values().forEach(list -> list.sort(String::compareToIgnoreCase));
        nameIndex.clear();
        nameIndex.putAll(idx);
    }

    // --- helpers ---
    private boolean isAllowed(String fileLower) {
        int dot = fileLower.lastIndexOf('.');
        String ext = (dot >= 0) ? fileLower.substring(dot+1) : "";
        return ALLOWED.contains(ext);
    }

    private Optional<String> scanOnceByFilename(String fileLower) {
        try (var walk = Files.walk(originalsRoot)) {
            return walk.filter(Files::isRegularFile)
                    .map(p -> originalsRoot.relativize(p.toAbsolutePath().normalize()).toString().replace('\\','/'))
                    .filter(rel -> fileName(rel).equalsIgnoreCase(fileLower))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static String normalizeRel(String p) { return p.replace('\\','/'); }
    private static String fileName(String rel) {
        int slash = rel.lastIndexOf('/');
        return (slash >= 0) ? rel.substring(slash+1) : rel;
    }
    private static String parentDir(String rel) {
        int slash = rel.lastIndexOf('/');
        return (slash >= 0) ? rel.substring(0, slash) : "";
    }
}
