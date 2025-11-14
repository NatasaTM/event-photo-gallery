package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActiveTrueOrderByNameAsc();
}
