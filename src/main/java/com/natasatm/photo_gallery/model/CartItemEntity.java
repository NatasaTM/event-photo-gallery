package com.natasatm.photo_gallery.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author Natasa Todorov Markovic
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cartitem_cart_preview",
                        columnNames = {"cart_id", "preview_rel_path"}
                )
        }
)
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Jedan cart ima više stavki
    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // "A/DSC_0001.jpg" - ključ za povezivanje sa preview/OriginalLocator
    @Column(name = "preview_rel_path", nullable = false, length = 512)
    private String previewRelPath;

    // Količina - min 1 (0 znači da stavka ne postoji, tj. briše se)
    @Column(nullable = false)
    private Integer quantity;

    // Cena po komadu u trenutku računanja
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // unitPrice * quantity
    @Column(name = "line_total", precision = 10, scale = 2)
    private BigDecimal lineTotal;
}
