package com.natasatm.photo_gallery.model;

import org.springframework.stereotype.Service;

/**
 * @author Natasa Todorov Markovic
 */

// 1) Za UI (preview). Dodali smo relPath da znaš podfolder + ime.
public record GalleryItem(
        String name,          // npr. "DSC_0001.jpg"
        String url,           // npr. "/images/A/DSC_0001.jpg?v=..."
        String previewRelPath,// npr. "A/DSC_0001.jpg"  ← KLJUČ ZA CHECKOUT
        long mtime
) {}
