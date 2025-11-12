package com.natasatm.photo_gallery.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * @author Natasa Todorov Markovic
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String place;

    @Column
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_currency_id", nullable = false)
    private Currency primaryCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_currency_id")
    private Currency secondaryCurrency;
}
