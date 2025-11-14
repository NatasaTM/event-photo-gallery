package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.dto.UserDto;
import com.natasatm.photo_gallery.model.User;
import com.natasatm.photo_gallery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Natasa Todorov Markovic
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthInfoController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserDto me(Authentication auth) {

        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        return UserDto.fromEntity(user);
    }
}
