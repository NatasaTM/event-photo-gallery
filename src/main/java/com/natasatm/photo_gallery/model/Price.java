package com.natasatm.photo_gallery.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author Natasa Todorov Markovic
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "price",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_price_product_list", columnNames={"product_id","price_list_id"})
        },
        indexes = {
                @Index(name="ix_price_product", columnList="product_id"),
                @Index(name="ix_price_list", columnList="price_list_id")
        })
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

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Column(nullable = false)
    private Integer taxRate = 0;  // izraženo u procentima (npr. 10, 18, 20)

    @Column(nullable = false)
    private Integer minQty = 1;

}
