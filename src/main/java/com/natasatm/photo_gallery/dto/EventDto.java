package com.natasatm.photo_gallery.dto;

import com.natasatm.photo_gallery.model.Event;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class EventDto {

    private Long id;
    private String name;
    private String place;
    private LocalDate date;
    private String primaryCurrencyCode;
    private String secondaryCurrencyCode;

    public static EventDto fromEntity(Event e) {
        EventDto dto = new EventDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setPlace(e.getPlace());
        dto.setDate(e.getDate());

        if (e.getPrimaryCurrency() != null) {
            dto.setPrimaryCurrencyCode(e.getPrimaryCurrency().getCode());
        }
        if (e.getSecondaryCurrency() != null) {
            dto.setSecondaryCurrencyCode(e.getSecondaryCurrency().getCode());
        }

        return dto;
    }
}
