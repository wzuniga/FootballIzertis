package com.izertis.football.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utility for generating and validating JWT tokens.
 *
 * <p>Uses HMAC-SHA256 (HS256). The signing key is derived from the
 * {@code app.jwt.secret} property and must be at least 256 bits (32 chars).</p>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT for the authenticated club.
     *
     * @param username the club's email (subject claim)
     * @param clubId   the club's UUID (stored as a string claim)
     * @return signed JWT string
     */
    public String generateToken(String username, UUID clubId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("clubId", clubId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /** Extracts the username (subject) from a valid token. */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extracts the clubId custom claim from a valid token. */
    public UUID getClubIdFromToken(String token) {
        String claim = (String) parseClaims(token).get("clubId");
        return UUID.fromString(claim);
    }

    /**
     * Validates a token, returning {@code true} if it is well-formed, signed correctly,
     * and not expired.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
