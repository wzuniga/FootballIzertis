package com.izertis.football.mapper;

import com.izertis.football.domain.Club;
import com.izertis.football.domain.Player;
import com.izertis.football.dto.request.PlayerRequest;
import com.izertis.football.dto.response.PlayerDetailResponse;
import com.izertis.football.dto.response.PlayerSummaryResponse;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link Player} domain objects and their DTO representations.
 */
@Component
public class PlayerMapper {

    public Player toEntity(PlayerRequest request, Club club) {
        return Player.builder()
                .givenName(request.getGivenName())
                .familyName(request.getFamilyName())
                .nationality(request.getNationality())
                .email(request.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .club(club)
                .build();
    }

    public PlayerSummaryResponse toSummaryResponse(Player player) {
        PlayerSummaryResponse dto = new PlayerSummaryResponse();
        dto.setId(player.getId());
        dto.setGivenName(player.getGivenName());
        dto.setFamilyName(player.getFamilyName());
        return dto;
    }

    public PlayerDetailResponse toDetailResponse(Player player) {
        PlayerDetailResponse dto = new PlayerDetailResponse();
        dto.setId(player.getId());
        dto.setGivenName(player.getGivenName());
        dto.setFamilyName(player.getFamilyName());
        dto.setNationality(player.getNationality());
        dto.setEmail(player.getEmail());
        dto.setDateOfBirth(player.getDateOfBirth());
        dto.setClubId(player.getClub().getId());
        return dto;
    }
}
