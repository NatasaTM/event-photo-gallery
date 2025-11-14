package com.natasatm.photo_gallery.dto;

import lombok.Data;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class CreateOrUpdatePriceListRequest {

    private String name;
    private String currencyCode; // "RSD" / "EUR"
    private Boolean isDefault;
}
