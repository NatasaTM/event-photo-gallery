package com.natasatm.photo_gallery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Natasa Todorov Markovic
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // LAN app, admin panel - možemo za sad ovako
                .authorizeHttpRequests(auth -> auth
                        // javno: galerija za kupce + slike + SSE
                        .requestMatchers(
                                "/",                // root
                                "/index.html",      // ako ručno serviraš ovo
                                "/gallery/**",      // ako imaš neku path za galeriju
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**",       // zavisi gde ti je frontend build
                                "/api/gallery/**",
                                "/sse/**",
                                "/api/cart/**",
                                "/login.html"
                        ).permitAll()

                        // budući izveštaji (ADMIN only) - za sad možda ne postoji, ali spremno:
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        // sve admin/seller funkcije (cenovnici, proizvodi, porudžbine)
                        // SELLER + ADMIN: konfiguracija proizvoda i cena
                        .requestMatchers(
                                "/api/products/**",
                                "/api/product-types/**",
                                "/api/price-lists/**",
                                "/api/prices/**",
                                "/api/events/**",
                                "/api/orders/**"
                        ).hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/auth/me").authenticated()


                        // sve ostalo traži login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")                   // 👈 gde je login stranica (GET)
                        .loginProcessingUrl("/login")          // 👈 POST ovde ide iz forme
                        .defaultSuccessUrl("/management.html", true) // 👈 posle logina, gde da ode (za sada na index)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
