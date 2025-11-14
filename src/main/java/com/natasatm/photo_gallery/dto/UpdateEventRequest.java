package com.natasatm.photo_gallery.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class UpdateEventRequest {

    private String name;
    private String place;
    private LocalDate date;
    private String primaryCurrencyCode;
    private String secondaryCurrencyCode;
}
