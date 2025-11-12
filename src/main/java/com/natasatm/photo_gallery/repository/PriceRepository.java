package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Natasa Todorov Markovic
 */
public interface PriceRepository extends JpaRepository<Price, Long> {
}
