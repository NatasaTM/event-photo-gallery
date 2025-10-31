package com.natasatm.photo_gallery.model;

import org.springframework.stereotype.Service;

/**
 * @author Natasa Todorov Markovic
 */

public record GalleryItem(String name, String url, long mtime) {

}
