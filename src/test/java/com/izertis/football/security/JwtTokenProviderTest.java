package com.izertis.football.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider unit tests")
class JwtTokenProviderTest {

    private static final String SECRET =
            "TestSecretKeyThatIsAtLeast256BitsLongForTestingPurposesOnly!!";
    private static final long EXPIRATION = 3_600_000L;

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, EXPIRATION);

    @Test
    @DisplayName("generateToken creates a non-blank token")
    void generateToken_producesNonBlankToken() {
        String token = provider.generateToken("test@club.com", UUID.randomUUID());
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("getUsernameFromToken returns correct subject")
    void getUsernameFromToken_returnsSubject() {
        String token = provider.generateToken("test@club.com", UUID.randomUUID());
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("test@club.com");
    }

    @Test
    @DisplayName("getClubIdFromToken returns correct clubId claim")
    void getClubIdFromToken_returnsClubId() {
        UUID clubId = UUID.randomUUID();
        String token = provider.generateToken("test@club.com", clubId);
        assertThat(provider.getClubIdFromToken(token)).isEqualTo(clubId);
    }

    @Test
    @DisplayName("validateToken returns true for a valid token")
    void validateToken_trueForValidToken() {
        String token = provider.generateToken("test@club.com", UUID.randomUUID());
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken returns false for a tampered token")
    void validateToken_falseForTamperedToken() {
        String token = provider.generateToken("test@club.com", UUID.randomUUID());
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for blank input")
    void validateToken_falseForBlank() {
        assertThat(provider.validateToken("")).isFalse();
    }
}
