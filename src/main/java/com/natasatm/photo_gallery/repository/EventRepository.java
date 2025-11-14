package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Natasa Todorov Markovic
 */
public interface EventRepository extends JpaRepository<Event,Long> {

    List<Event> findByActiveTrueOrderByDateDesc();
}
