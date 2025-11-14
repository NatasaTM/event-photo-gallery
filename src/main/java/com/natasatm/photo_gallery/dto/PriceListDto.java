package com.natasatm.photo_gallery.dto;

import com.natasatm.photo_gallery.model.PriceList;
import lombok.Data;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class PriceListDto {

    private Long id;
    private String name;
    private String currencyCode;
    private Boolean isDefault;

    public static PriceListDto fromEntity(PriceList pl) {
        PriceListDto dto = new PriceListDto();
        dto.setId(pl.getId());
        dto.setName(pl.getName());
        dto.setCurrencyCode(pl.getCurrency().getCode());
        dto.setIsDefault(pl.getIsDefault());
        return dto;
    }
}
