package com.natasatm.photo_gallery.dto;

import com.natasatm.photo_gallery.model.Product;
import lombok.Data;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Long productTypeId;
    private String productTypeName;

    public static ProductDto fromEntity(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setIsActive(p.getIsActive());
        dto.setProductTypeId(p.getProductType().getId());
        dto.setProductTypeName(p.getProductType().getName());
        return dto;
    }
}
