package com.natasatm.photo_gallery.data;

import com.natasatm.photo_gallery.model.Role;
import com.natasatm.photo_gallery.model.User;
import com.natasatm.photo_gallery.repository.RoleRepository;
import com.natasatm.photo_gallery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Natasa Todorov Markovic
 */
@Configuration
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {

            // --- ROLES ---
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            new Role(null, "ROLE_ADMIN", new HashSet<>())
                    ));

            Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                    .orElseGet(() -> roleRepository.save(
                            new Role(null, "ROLE_SELLER", new HashSet<>())
                    ));

            // --- ADMIN USER ---
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .name("System Administrator")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(new HashSet<>(Set.of(adminRole, sellerRole)))
                        .build();

                userRepository.save(admin);
                System.out.println("✔ Created default admin (admin/admin123)");
            }

            // --- SELLER #1 ---
            if (userRepository.findByUsername("seller1").isEmpty()) {
                User seller1 = User.builder()
                        .username("seller1")
                        .name("Seller One")
                        .password(passwordEncoder.encode("seller123"))
                        .roles(new HashSet<>(Set.of(sellerRole)))
                        .build();

                userRepository.save(seller1);
                System.out.println("✔ Created default seller1");
            }

            // --- SELLER #2 ---
            if (userRepository.findByUsername("seller2").isEmpty()) {
                User seller2 = User.builder()
                        .username("seller2")
                        .name("Seller Two")
                        .password(passwordEncoder.encode("seller123"))
                        .roles(new HashSet<>(Set.of(sellerRole)))
                        .build();

                userRepository.save(seller2);
                System.out.println("✔ Created default seller2");
            }
        };
    }
}
