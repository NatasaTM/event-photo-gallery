package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Natasa Todorov Markovic
 */
public interface EventRepository extends JpaRepository<Event,Long> {
}
