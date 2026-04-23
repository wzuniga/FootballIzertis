package com.izertis.football.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PUT /club/{clubId} (update club details).
 * All fields are optional – only non-null values will be applied.
 */
@Data
@Schema(description = "Payload to update an existing Football Club")
public class ClubUpdateRequest {

    @NotBlank(message = "officialName is required")
    @Schema(description = "Official registered name of the club", example = "FC Barcelona S.A.D.")
    private String officialName;

    @NotBlank(message = "popularName is required")
    @Schema(description = "Commonly-known name of the club", example = "Barça")
    private String popularName;

    @NotBlank(message = "federation is required")
    @Size(max = 8, message = "federation acronym must be at most 8 characters")
    @Schema(description = "Federation acronym (max 8 chars)", example = "RFEF")
    private String federation;

    @Schema(description = "Whether club details are publicly visible to other users", example = "false")
    private Boolean publicVisible;
}
