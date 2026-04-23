package com.izertis.football.service;

import com.izertis.football.domain.Club;
import com.izertis.football.dto.request.ClubRegistrationRequest;
import com.izertis.football.dto.request.ClubUpdateRequest;
import com.izertis.football.dto.response.ClubDetailResponse;
import com.izertis.football.dto.response.ClubSummaryResponse;
import com.izertis.football.exception.AccessDeniedException;
import com.izertis.football.exception.DuplicateResourceException;
import com.izertis.football.exception.ResourceNotFoundException;
import com.izertis.football.mapper.ClubMapper;
import com.izertis.football.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for Club management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    private final ClubRepository clubRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClubMapper clubMapper;

    /**
     * Registers a new club. The username must be unique system-wide.
     *
     * @param request registration payload
     * @return the saved club entity
     */
    @Transactional
    public Club register(ClubRegistrationRequest request) {
        if (clubRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "A club with username '" + request.getUsername() + "' already exists");
        }
        Club club = clubMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
        return clubRepository.save(club);
    }

    /**
     * Returns the list of publicly visible clubs, optionally filtered by name and federation.
     *
     * @param name       partial match against officialName or popularName (optional)
     * @param federation exact match against federation acronym (optional)
     */
    public List<ClubSummaryResponse> getPublicClubs(String name, String federation) {
        return clubRepository.searchPublicClubs(name, federation).stream()
                .map(clubMapper::toSummaryResponse)
                .toList();
    }

    /**
     * Returns the full detail of a specific club.
     * <ul>
     *   <li>If the club is public → any authenticated user may view it.</li>
     *   <li>If the club is private → only the owning club may view it.</li>
     * </ul>
     *
     * @param clubId          target club ID
     * @param requestingClubId the ID of the currently authenticated club
     */
    public ClubDetailResponse getClubDetail(UUID clubId, UUID requestingClubId) {
        Club club = findClubOrThrow(clubId);
        if (!club.isPublicVisible() && !club.getId().equals(requestingClubId)) {
            throw new AccessDeniedException("You do not have permission to view this club");
        }
        return clubMapper.toDetailResponse(club);
    }

    /**
     * Updates a club's details. Only the owning club can update its own data.
     *
     * @param clubId          target club ID
     * @param request         fields to update
     * @param requestingClubId the ID of the currently authenticated club
     */
    @Transactional
    public ClubDetailResponse updateClub(UUID clubId, ClubUpdateRequest request, UUID requestingClubId) {
        Club club = findClubOrThrow(clubId);
        if (!club.getId().equals(requestingClubId)) {
            throw new AccessDeniedException("You can only update your own club");
        }
        club.setOfficialName(request.getOfficialName());
        club.setPopularName(request.getPopularName());
        club.setFederation(request.getFederation());
        if (request.getPublicVisible() != null) {
            club.setPublicVisible(request.getPublicVisible());
        }
        return clubMapper.toDetailResponse(clubRepository.save(club));
    }

    /** Convenience method used by other services. */
    public Club findClubOrThrow(UUID clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + clubId));
    }

    /** Resolves a club's UUID from its username (email). */
    public UUID getClubIdByUsername(String username) {
        return clubRepository.findByUsername(username)
                .map(Club::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + username));
    }
}
