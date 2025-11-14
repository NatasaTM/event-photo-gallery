package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.EventPriceList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
public interface EventPriceListRepository extends JpaRepository<EventPriceList, Long> {

    List<EventPriceList> findByEventIdAndActiveTrueOrderByPriorityDesc(Long eventId);
}
