package com.izertis.football.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

/**
 * Reduced club summary used in GET /club/ list results.
 * Excludes username, password, and player count as per spec.
 */
@Data
@Schema(description = "Summary of a public Football Club (list view)")
public class ClubSummaryResponse {

    @Schema(description = "Internal club identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Official registered name", example = "FC Barcelona")
    private String officialName;

    @Schema(description = "Commonly-known name", example = "Barça")
    private String popularName;

    @Schema(description = "Federation acronym", example = "RFEF")
    private String federation;
}
