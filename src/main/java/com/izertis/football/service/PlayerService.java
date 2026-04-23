package com.izertis.football.service;

import com.izertis.football.domain.Club;
import com.izertis.football.domain.Player;
import com.izertis.football.dto.request.PlayerRequest;
import com.izertis.football.dto.response.PlayerDetailResponse;
import com.izertis.football.dto.response.PlayerSummaryResponse;
import com.izertis.football.exception.AccessDeniedException;
import com.izertis.football.exception.DuplicateResourceException;
import com.izertis.football.exception.ResourceNotFoundException;
import com.izertis.football.mapper.PlayerMapper;
import com.izertis.football.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for Player management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final ClubService clubService;
    private final PlayerMapper playerMapper;

    /**
     * Creates a new player under the specified club.
     * Only the owning club may add players.
     */
    @Transactional
    public PlayerDetailResponse createPlayer(UUID clubId, PlayerRequest request, UUID requestingClubId) {
        Club club = clubService.findClubOrThrow(clubId);
        assertOwnership(club, requestingClubId, "You can only add players to your own club");

        if (playerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "A player with email '" + request.getEmail() + "' already exists");
        }

        Player saved = playerRepository.save(playerMapper.toEntity(request, club));
        return playerMapper.toDetailResponse(saved);
    }

    /**
     * Lists the players of a club, optionally filtered by name and nationality.
     * Access rules: public club → any authenticated user; private club → owner only.
     *
     * @param name        partial match against givenName or familyName (optional)
     * @param nationality exact match against nationality (optional)
     */
    public List<PlayerSummaryResponse> listPlayers(UUID clubId, UUID requestingClubId,
                                                    String name, String nationality) {
        Club club = clubService.findClubOrThrow(clubId);
        assertReadAccess(club, requestingClubId);

        return playerRepository.searchByClub(clubId, name, nationality).stream()
                .map(playerMapper::toSummaryResponse)
                .toList();
    }

    /**
     * Returns the full details of a single player.
     * Access rules: public club → any authenticated user; private club → owner only.
     */
    public PlayerDetailResponse getPlayerDetail(UUID clubId, UUID playerId, UUID requestingClubId) {
        Club club = clubService.findClubOrThrow(clubId);
        assertReadAccess(club, requestingClubId);

        Player player = playerRepository.findByIdAndClubId(playerId, clubId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Player not found with id " + playerId + " in club " + clubId));
        return playerMapper.toDetailResponse(player);
    }

    /**
     * Updates a player's data. Only the owning club can update its players.
     */
    @Transactional
    public PlayerDetailResponse updatePlayer(UUID clubId, UUID playerId,
                                             PlayerRequest request, UUID requestingClubId) {
        Club club = clubService.findClubOrThrow(clubId);
        assertOwnership(club, requestingClubId, "You can only update players of your own club");

        Player player = playerRepository.findByIdAndClubId(playerId, clubId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Player not found with id " + playerId + " in club " + clubId));

        if (playerRepository.existsByEmailAndIdNot(request.getEmail(), playerId)) {
            throw new DuplicateResourceException(
                    "A player with email '" + request.getEmail() + "' already exists");
        }

        player.setGivenName(request.getGivenName());
        player.setFamilyName(request.getFamilyName());
        player.setNationality(request.getNationality());
        player.setEmail(request.getEmail());
        player.setDateOfBirth(request.getDateOfBirth());

        return playerMapper.toDetailResponse(playerRepository.save(player));
    }

    /**
     * Deletes a player. Only the owning club can delete its players.
     */
    @Transactional
    public void deletePlayer(UUID clubId, UUID playerId, UUID requestingClubId) {
        Club club = clubService.findClubOrThrow(clubId);
        assertOwnership(club, requestingClubId, "You can only delete players from your own club");

        Player player = playerRepository.findByIdAndClubId(playerId, clubId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Player not found with id " + playerId + " in club " + clubId));

        playerRepository.delete(player);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Throws 403 if the requesting club does not own the given club. */
    private void assertOwnership(Club club, UUID requestingClubId, String message) {
        if (!club.getId().equals(requestingClubId)) {
            throw new AccessDeniedException(message);
        }
    }

    /** Throws 403 if the club is private and the requesting club is not the owner. */
    private void assertReadAccess(Club club, UUID requestingClubId) {
        if (!club.isPublicVisible() && !club.getId().equals(requestingClubId)) {
            throw new AccessDeniedException("You do not have permission to view this club's players");
        }
    }
}
