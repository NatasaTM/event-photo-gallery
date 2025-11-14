package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Natasa Todorov Markovic
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
