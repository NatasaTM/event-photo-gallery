package com.natasatm.photo_gallery.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Natasa Todorov Markovic
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
        name = "event_price_list",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_event_price_list", columnNames={"event_id","price_list_id"})
        }
)
public class EventPriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    private PriceList priceList;

    // Meta-polja koja su ti korisna:
    private Boolean active = true;              // da li je trenutno važeći za taj event
    private Boolean defaultForCurrency = false; // npr. default EUR ili RSD u okviru eventa
    private LocalDateTime validFrom;            // opcionalno
    private LocalDateTime validTo;              // opcionalno
    private Integer priority = 0;               // u slučaju više važećih, biraj najveći
}
