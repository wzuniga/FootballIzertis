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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService unit tests")
class PlayerServiceTest {

    @Mock PlayerRepository playerRepository;
    @Mock ClubService clubService;
    @Mock PlayerMapper playerMapper;

    @InjectMocks PlayerService playerService;

    private Club ownerClub;
    private Club otherClub;
    private Player player;
    private PlayerRequest playerRequest;

    private final UUID ownerClubId  = UUID.randomUUID();
    private final UUID otherClubId  = UUID.randomUUID();
    private final UUID playerId     = UUID.randomUUID();
    private final UUID unknownId    = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ownerClub = Club.builder().id(ownerClubId).publicVisible(true).build();
        otherClub = Club.builder().id(otherClubId).publicVisible(false).build();

        player = Player.builder()
                .id(playerId).givenName("Lionel").familyName("Messi")
                .nationality("Argentine").email("messi@club.com")
                .dateOfBirth(LocalDate.of(1987, 6, 24))
                .club(ownerClub).build();

        playerRequest = new PlayerRequest();
        playerRequest.setGivenName("Lionel");
        playerRequest.setFamilyName("Messi");
        playerRequest.setNationality("Argentine");
        playerRequest.setEmail("messi@club.com");
        playerRequest.setDateOfBirth(LocalDate.of(1987, 6, 24));
    }

    // --- createPlayer -------------------------------------------------------

    @Test
    @DisplayName("createPlayer: creates player for owning club")
    void createPlayer_success() {
        PlayerDetailResponse detail = new PlayerDetailResponse();
        when(clubService.findClubOrThrow(ownerClubId)).thenReturn(ownerClub);
        when(playerRepository.existsByEmail("messi@club.com")).thenReturn(false);
        when(playerMapper.toEntity(playerRequest, ownerClub)).thenReturn(player);
        when(playerRepository.save(player)).thenReturn(player);
        when(playerMapper.toDetailResponse(player)).thenReturn(detail);

        PlayerDetailResponse result = playerService.createPlayer(ownerClubId, playerRequest, ownerClubId);

        assertThat(result).isSameAs(detail);
    }

    @Test
    @DisplayName("createPlayer: throws 403 when not the owner")
    void createPlayer_throws_whenNotOwner() {
        when(clubService.findClubOrThrow(ownerClubId)).thenReturn(ownerClub);

        assertThatThrownBy(() -> playerService.createPlayer(ownerClubId, playerRequest, otherClubId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("createPlayer: throws 409 when email already used")
    void createPlayer_throws_whenEmailDuplicate() {
        when(clubService.findClubOrThrow(ownerClubId)).thenReturn(ownerClub);
        when(playerRepository.existsByEmail("messi@club.com")).thenReturn(true);

        assertThatThrownBy(() -> playerService.createPlayer(ownerClubId, playerRequest, ownerClubId))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // --- listPlayers --------------------------------------------------------

    @Test
    @DisplayName("listPlayers: public club accessible to any authenticated user")
    void listPlayers_publicClub_anyUser() {
        PlayerSummaryResponse summary = new PlayerSummaryResponse();
        when(clubService.findClubOrThrow(ownerClubId)).thenReturn(ownerClub);
        when(playerRepository.searchByClub(ownerClubId, null, null)).thenReturn(List.of(player));
        when(playerMapper.toSummaryResponse(player)).thenReturn(summary);

        List<PlayerSummaryResponse> result = playerService.listPlayers(ownerClubId, unknownId, null, null);

        assertThat(result).containsExactly(summary);
    }

    @Test
    @DisplayName("listPlayers: private club throws 403 for non-owner")
    void listPlayers_privateClub_throwsForNonOwner() {
        when(clubService.findClubOrThrow(otherClubId)).thenReturn(otherClub);

        assertThatThrownBy(() -> playerService.listPlayers(otherClubId, ownerClubId, null, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --- deletePlayer -------------------------------------------------------

    @Test
    @DisplayName("deletePlayer: deletes when owner requests")
    void deletePlayer_success() {
        when(clubService.findClubOrThrow(ownerClubId)).thenReturn(ownerClub);
        when(playerRepository.findByIdAndClubId(playerId, ownerClubId)).thenReturn(Optional.of(player));

        playerService.deletePlayer(ownerClubId, playerId, ownerClubId);

        verify(playerRepository).delete(player);
    }

    @Test
    @DisplayName("deletePlayer: throws 404 when player not in club")
    void deletePlayer_throws_whenPlayerNotFound() {
        when(clubService.findClubOrThrow(ownerClubId)).thenReturn(ownerClub);
        when(playerRepository.findByIdAndClubId(unknownId, ownerClubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.deletePlayer(ownerClubId, unknownId, ownerClubId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
