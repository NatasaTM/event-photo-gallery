package com.natasatm.photo_gallery.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class CreateEventRequest {

    private String name;
    private String place;
    private LocalDate date;

    // valuta dolazi kao kod (RSD/EUR)
    private String primaryCurrencyCode;
    private String secondaryCurrencyCode; // može biti null
}
