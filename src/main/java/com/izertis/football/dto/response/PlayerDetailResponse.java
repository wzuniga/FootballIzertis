package com.izertis.football.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Full player details response (used for GET /club/{clubId}/player/{playerId}).
 */
@Data
@Schema(description = "Full details of a Player")
public class PlayerDetailResponse {

    @Schema(description = "Internal player identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Player's given name", example = "Lionel")
    private String givenName;

    @Schema(description = "Player's family name", example = "Messi")
    private String familyName;

    @Schema(description = "Player's nationality", example = "Argentine")
    private String nationality;

    @Schema(description = "Player's email", example = "l.messi@club.com")
    private String email;

    @Schema(description = "Player's date of birth (YYYY-MM-DD)", example = "1987-06-24")
    private LocalDate dateOfBirth;

    @Schema(description = "ID of the club this player belongs to (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID clubId;
}
