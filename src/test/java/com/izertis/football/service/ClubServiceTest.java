package com.izertis.football.service;

import com.izertis.football.domain.Club;
import com.izertis.football.dto.request.ClubRegistrationRequest;
import com.izertis.football.dto.response.ClubDetailResponse;
import com.izertis.football.dto.response.ClubSummaryResponse;
import com.izertis.football.exception.AccessDeniedException;
import com.izertis.football.exception.DuplicateResourceException;
import com.izertis.football.exception.ResourceNotFoundException;
import com.izertis.football.mapper.ClubMapper;
import com.izertis.football.repository.ClubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClubService unit tests")
class ClubServiceTest {

    @Mock ClubRepository clubRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ClubMapper clubMapper;

    @InjectMocks ClubService clubService;

    private Club publicClub;
    private Club privateClub;

    private final UUID publicId  = UUID.randomUUID();
    private final UUID privateId = UUID.randomUUID();
    private final UUID otherId   = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        publicClub = Club.builder()
                .id(publicId).username("public@club.com").password("encoded")
                .officialName("Public FC").popularName("Public").federation("UEFA")
                .publicVisible(true).build();

        privateClub = Club.builder()
                .id(privateId).username("private@club.com").password("encoded")
                .officialName("Private FC").popularName("Private").federation("UEFA")
                .publicVisible(false).build();
    }

    // --- register -----------------------------------------------------------

    @Test
    @DisplayName("register: saves club when username is unique")
    void register_savesClub_whenUsernameIsNew() {
        ClubRegistrationRequest req = new ClubRegistrationRequest();
        req.setUsername("new@club.com");
        req.setPassword("password123");
        req.setOfficialName("New FC");
        req.setPopularName("New");
        req.setFederation("UEFA");
        req.setPublicVisible(true);

        when(clubRepository.existsByUsername("new@club.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(clubMapper.toEntity(eq(req), eq("encoded"))).thenReturn(publicClub);
        when(clubRepository.save(publicClub)).thenReturn(publicClub);

        Club result = clubService.register(req);

        assertThat(result).isSameAs(publicClub);
        verify(clubRepository).save(publicClub);
    }

    @Test
    @DisplayName("register: throws DuplicateResourceException when username already exists")
    void register_throws_whenUsernameExists() {
        ClubRegistrationRequest req = new ClubRegistrationRequest();
        req.setUsername("public@club.com");

        when(clubRepository.existsByUsername("public@club.com")).thenReturn(true);

        assertThatThrownBy(() -> clubService.register(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // --- getPublicClubs -----------------------------------------------------

    @Test
    @DisplayName("getPublicClubs: returns only public clubs")
    void getPublicClubs_returnsPublicOnly() {
        ClubSummaryResponse summary = new ClubSummaryResponse();
        when(clubRepository.searchPublicClubs(null, null)).thenReturn(List.of(publicClub));
        when(clubMapper.toSummaryResponse(publicClub)).thenReturn(summary);

        List<ClubSummaryResponse> result = clubService.getPublicClubs(null, null);

        assertThat(result).containsExactly(summary);
    }

    // --- getClubDetail ------------------------------------------------------

    @Test
    @DisplayName("getClubDetail: public club visible to any authenticated user")
    void getClubDetail_publicClub_visibleToAnyUser() {
        ClubDetailResponse detail = new ClubDetailResponse();
        when(clubRepository.findById(publicId)).thenReturn(Optional.of(publicClub));
        when(clubMapper.toDetailResponse(publicClub)).thenReturn(detail);

        ClubDetailResponse result = clubService.getClubDetail(publicId, otherId);

        assertThat(result).isSameAs(detail);
    }

    @Test
    @DisplayName("getClubDetail: private club visible to owner")
    void getClubDetail_privateClub_visibleToOwner() {
        ClubDetailResponse detail = new ClubDetailResponse();
        when(clubRepository.findById(privateId)).thenReturn(Optional.of(privateClub));
        when(clubMapper.toDetailResponse(privateClub)).thenReturn(detail);

        ClubDetailResponse result = clubService.getClubDetail(privateId, privateId);

        assertThat(result).isSameAs(detail);
    }

    @Test
    @DisplayName("getClubDetail: private club throws 403 for non-owner")
    void getClubDetail_privateClub_throwsForNonOwner() {
        when(clubRepository.findById(privateId)).thenReturn(Optional.of(privateClub));

        assertThatThrownBy(() -> clubService.getClubDetail(privateId, otherId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getClubDetail: throws 404 when club does not exist")
    void getClubDetail_throws404_whenNotFound() {
        when(clubRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.getClubDetail(
                UUID.fromString("00000000-0000-0000-0000-000000000000"), publicId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
