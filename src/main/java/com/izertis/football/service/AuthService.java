package com.izertis.football.service;

import com.izertis.football.dto.request.LoginRequest;
import com.izertis.football.dto.response.LoginResponse;
import com.izertis.football.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Handles authentication and JWT issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ClubService clubService;

    /**
     * Authenticates the club using Spring Security's {@link AuthenticationManager}
     * and returns a signed JWT containing the club's email and database ID.
     *
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = authentication.getName();
        UUID clubId = clubService.getClubIdByUsername(username);
        String token = tokenProvider.generateToken(username, clubId);

        return new LoginResponse(token);
    }
}
