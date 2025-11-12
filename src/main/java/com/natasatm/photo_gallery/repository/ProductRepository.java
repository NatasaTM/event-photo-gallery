package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Natasa Todorov Markovic
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
