package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Natasa Todorov Markovic
 */
public interface CurrencyRepository extends JpaRepository<Currency,Long> {
}
