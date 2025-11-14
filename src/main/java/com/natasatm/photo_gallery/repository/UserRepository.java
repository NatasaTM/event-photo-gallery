package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Natasa Todorov Markovic
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
