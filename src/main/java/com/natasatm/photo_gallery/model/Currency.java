package com.natasatm.photo_gallery.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author Natasa Todorov Markovic
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., Euro, Dollar

    @Column(nullable = false, unique = true)
    private String code; // e.g., EUR, USD

    @Column
    private String symbol; // e.g., €, $

}
