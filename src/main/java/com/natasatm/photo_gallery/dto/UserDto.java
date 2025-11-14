package com.natasatm.photo_gallery.dto;

import com.natasatm.photo_gallery.model.Role;
import com.natasatm.photo_gallery.model.User;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Natasa Todorov Markovic
 */
@Data
public class UserDto {

    private Long id;
    private String username;
    private String name;
    private Set<String> roles;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setRoles(
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
        return dto;
    }
}
