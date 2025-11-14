package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Natasa Todorov Markovic
 */
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    Optional<ProductType> findByName(String name);

    List<ProductType> findAllByOrderByNameAsc();
}
