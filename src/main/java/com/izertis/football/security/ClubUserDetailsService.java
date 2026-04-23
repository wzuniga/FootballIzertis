package com.izertis.football.security;

import com.izertis.football.domain.Club;
import com.izertis.football.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Loads a Club by its username (email) so Spring Security can perform authentication.
 */
@Service
@RequiredArgsConstructor
public class ClubUserDetailsService implements UserDetailsService {

    private final ClubRepository clubRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Club club = clubRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Club not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(club.getUsername())
                .password(club.getPassword())
                .roles("CLUB")
                .build();
    }
}
