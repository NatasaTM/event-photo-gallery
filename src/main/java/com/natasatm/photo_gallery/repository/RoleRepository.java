package com.natasatm.photo_gallery.repository;

import com.natasatm.photo_gallery.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Natasa Todorov Markovic
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
}
