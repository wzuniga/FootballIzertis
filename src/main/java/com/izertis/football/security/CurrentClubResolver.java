package com.izertis.football.security;

import com.izertis.football.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility to extract information about the club that sent the current request.
 */
@Component
@RequiredArgsConstructor
public class CurrentClubResolver {

    private final ClubRepository clubRepository;

    /**
     * Returns the UUID of the currently authenticated club.
     */
    public UUID getCurrentClubId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return clubRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated club not found: " + username))
                .getId();
    }
}
