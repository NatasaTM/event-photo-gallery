package com.natasatm.photo_gallery.dto;

import com.natasatm.photo_gallery.model.Price;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class PriceDto {

    private Long id;
    private Long productId;
    private String productName;
    private Long priceListId;
    private BigDecimal priceAmount;
    private Integer taxRate;
    private Integer minQty;

    public static PriceDto fromEntity(Price p) {
        PriceDto dto = new PriceDto();
        dto.setId(p.getId());
        dto.setProductId(p.getProduct().getId());
        dto.setProductName(p.getProduct().getName());
        dto.setPriceListId(p.getPriceList().getId());
        dto.setPriceAmount(p.getPriceAmount());
        dto.setTaxRate(p.getTaxRate());
        dto.setMinQty(p.getMinQty());
        return dto;
    }
}
