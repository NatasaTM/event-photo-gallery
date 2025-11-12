package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Natasa Todorov Markovic
 */
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
}
