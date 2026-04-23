package com.izertis.football.config;

import com.izertis.football.security.JwtAuthenticationEntryPoint;
import com.izertis.football.security.JwtAuthenticationFilter;
import com.izertis.football.security.ClubUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <ul>
 *   <li>Stateless session (JWT-based)</li>
 *   <li>CSRF disabled (REST API)</li>
 *   <li>Public endpoints: POST /club, POST /login, Swagger UI, OpenAPI docs</li>
 *   <li>All other endpoints require a valid JWT</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClubUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(authenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                    // Club registration and login are public
                    .requestMatchers(HttpMethod.POST, "/club").permitAll()
                    .requestMatchers(HttpMethod.POST, "/login").permitAll()
                    // Swagger / OpenAPI docs are public
                    .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/api-docs/**",
                            "/v3/api-docs/**"
                    ).permitAll()
                    // H2 console (dev only)
                    .requestMatchers("/h2-console/**").permitAll()
                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Allow H2 console frames in dev
            .headers(headers ->
                    headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
