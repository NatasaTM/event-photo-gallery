package com.natasatm.photo_gallery.dto;

import lombok.Data;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class LinkPriceListToEventRequest {
    private Long priceListId;
    private Boolean defaultForCurrency;
}
