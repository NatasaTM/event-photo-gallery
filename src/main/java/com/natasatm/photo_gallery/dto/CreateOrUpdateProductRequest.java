package com.natasatm.photo_gallery.dto;

import lombok.Data;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class CreateOrUpdateProductRequest {

    private String name;
    private String description;
    private Long productTypeId;
}
