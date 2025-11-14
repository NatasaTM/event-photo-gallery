package com.natasatm.photo_gallery.dto;

import com.natasatm.photo_gallery.model.EventPriceList;
import lombok.Data;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class EventPriceListDto {

    private Long id;
    private Long eventId;
    private Long priceListId;
    private String priceListName;
    private String currencyCode;
    private Boolean defaultForCurrency;
    private Boolean active;

    public static EventPriceListDto fromEntity(EventPriceList epl) {
        EventPriceListDto dto = new EventPriceListDto();
        dto.setId(epl.getId());
        dto.setEventId(epl.getEvent().getId());
        dto.setPriceListId(epl.getPriceList().getId());
        dto.setPriceListName(epl.getPriceList().getName());
        dto.setCurrencyCode(epl.getPriceList().getCurrency().getCode());
        dto.setDefaultForCurrency(epl.getDefaultForCurrency());
        dto.setActive(epl.getActive());
        return dto;
    }
}
