package com.izertis.football.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request body for POST /club (club registration).
 */
@Data
@Schema(description = "Payload to register a new Football Club")
public class ClubRegistrationRequest {

    @NotBlank(message = "username is required")
    @Email(regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
           message = "username must be a valid email address")
    @Schema(description = "Club login email (must be unique)", example = "info@townfc.com")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters long")
    @Schema(description = "Password (min 8 chars)", example = "iTk19!n.")
    private String password;

    @NotBlank(message = "officialName is required")
    @Schema(description = "Official registered name of the club", example = "FC Barcelona")
    private String officialName;

    @NotBlank(message = "popularName is required")
    @Schema(description = "Commonly-known name of the club", example = "Barça")
    private String popularName;

    @NotBlank(message = "federation is required")
    @Size(max = 8, message = "federation acronym must be at most 8 characters")
    @Schema(description = "Federation acronym the club belongs to (max 8 chars)", example = "RFEF")
    private String federation;

    @NotNull(message = "public is required")
    @Schema(description = "Whether club details are publicly visible to other users", example = "true")
    private Boolean publicVisible;
}
