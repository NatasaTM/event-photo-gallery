package com.natasatm.photo_gallery.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class CreateOrUpdatePriceRequest {

    private Long productId;
    private Long priceListId;
    private BigDecimal priceAmount;
    private Integer taxRate;
    private Integer minQty;
}
