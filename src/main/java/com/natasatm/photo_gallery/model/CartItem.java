package com.natasatm.photo_gallery.model;

/**
 * @author Natasa Todorov Markovic
 */
// 2) Za checkout (frontend -> backend)
public record CartItem(
        String previewRelPath, // "A/DSC_0001.jpg"
        Long productId,        // npr. Digital file / Print 10x15
        Integer qty            // default 1
) { public CartItem { if (qty == null) qty = 1; } }
