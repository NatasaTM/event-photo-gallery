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
@Table(name = "price")
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(nullable = false, name = "price_list_id")
    private PriceList priceList;

    private Integer unitAmountMinor;

    private Integer taxRate;         // u %

    private Integer minQty = 1;

}
