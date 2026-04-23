package com.izertis.football.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

/**
 * Reduced player info used in GET /club/{clubId}/player list results.
 * Only id, givenName and familyName are exposed as per spec.
 */
@Data
@Schema(description = "Player summary (list view)")
public class PlayerSummaryResponse {

    @Schema(description = "Internal player identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Player's given name", example = "Lionel")
    private String givenName;

    @Schema(description = "Player's family name", example = "Messi")
    private String familyName;
}
