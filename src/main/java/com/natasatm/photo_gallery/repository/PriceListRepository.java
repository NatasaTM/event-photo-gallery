package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    List<PriceList> findAllByOrderByNameAsc();
}
