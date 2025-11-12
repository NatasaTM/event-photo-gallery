package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Natasa Todorov Markovic
 */
public interface PriceListRepository extends JpaRepository<PriceList, Long> {
}
