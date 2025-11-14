package com.natasatm.photo_gallery.dto;

import com.natasatm.photo_gallery.model.ProductType;
import lombok.Data;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class ProductTypeDto {

    private Long id;
    private String name;

    public static ProductTypeDto fromEntity(ProductType pt) {
        ProductTypeDto dto = new ProductTypeDto();
        dto.setId(pt.getId());
        dto.setName(pt.getName());
        return dto;
    }
}
