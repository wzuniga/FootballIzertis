package com.izertis.football.mapper;

import com.izertis.football.domain.Club;
import com.izertis.football.dto.request.ClubRegistrationRequest;
import com.izertis.football.dto.response.ClubDetailResponse;
import com.izertis.football.dto.response.ClubSummaryResponse;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link Club} domain objects and their DTO representations.
 */
@Component
public class ClubMapper {

    public Club toEntity(ClubRegistrationRequest request, String encodedPassword) {
        return Club.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .officialName(request.getOfficialName())
                .popularName(request.getPopularName())
                .federation(request.getFederation())
                .publicVisible(Boolean.TRUE.equals(request.getPublicVisible()))
                .build();
    }

    public ClubSummaryResponse toSummaryResponse(Club club) {
        ClubSummaryResponse dto = new ClubSummaryResponse();
        dto.setId(club.getId());
        dto.setOfficialName(club.getOfficialName());
        dto.setPopularName(club.getPopularName());
        dto.setFederation(club.getFederation());
        return dto;
    }

    public ClubDetailResponse toDetailResponse(Club club) {
        ClubDetailResponse dto = new ClubDetailResponse();
        dto.setId(club.getId());
        dto.setOfficialName(club.getOfficialName());
        dto.setPopularName(club.getPopularName());
        dto.setFederation(club.getFederation());
        dto.setPublicVisible(club.isPublicVisible());
        dto.setPlayerCount(club.getPlayers().size());
        return dto;
    }
}
