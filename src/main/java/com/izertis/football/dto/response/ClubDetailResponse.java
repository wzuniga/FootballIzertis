package com.izertis.football.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

/**
 * Full club details response (used for GET /club/{clubId}).
 * Does NOT include username, password, or raw player list.
 */
@Data
@Schema(description = "Full details of a Football Club")
public class ClubDetailResponse {

    @Schema(description = "Internal club identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Official registered name", example = "FC Barcelona")
    private String officialName;

    @Schema(description = "Commonly-known name", example = "Barça")
    private String popularName;

    @Schema(description = "Federation acronym", example = "RFEF")
    private String federation;

    @Schema(description = "Whether the club is publicly visible", example = "true")
    private boolean publicVisible;

    @Schema(description = "Number of federated players registered in this club", example = "25")
    private int playerCount;
}
