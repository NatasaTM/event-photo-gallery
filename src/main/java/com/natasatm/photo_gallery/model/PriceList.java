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
@Table(name = "price_list")
public class PriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(nullable = false, name = "currency_id")
    private Currency currency;

    @Column (columnDefinition = "BOOLEAN")
    private Boolean isDefault;
}
