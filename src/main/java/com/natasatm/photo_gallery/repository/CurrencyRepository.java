package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Natasa Todorov Markovic
 */
public interface CurrencyRepository extends JpaRepository<Currency,Long> {

    Optional<Currency> findByCode(String code);
}
