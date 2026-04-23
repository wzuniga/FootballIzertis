package com.izertis.football.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for POST /club/{clubId}/player (create player).
 */
@Data
@Schema(description = "Payload to register a new Player under a Club")
public class PlayerRequest {

    @NotBlank(message = "givenName is required")
    @Schema(description = "Player's given (first) name", example = "Lionel")
    private String givenName;

    @NotBlank(message = "familyName is required")
    @Schema(description = "Player's family (last) name", example = "Messi")
    private String familyName;

    @NotBlank(message = "nationality is required")
    @Schema(description = "Player's nationality", example = "Argentine")
    private String nationality;

    @NotBlank(message = "email is required")
    @Email(regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
           message = "email must be a valid email address")
    @Schema(description = "Player's email (must be unique in the system)", example = "l.messi@club.com")
    private String email;

    @NotNull(message = "dateOfBirth is required")
    @Past(message = "dateOfBirth must be a date in the past")
    @Schema(description = "Player's date of birth (ISO-8601: YYYY-MM-DD)", example = "1987-06-24", type = "string", format = "date")
    private LocalDate dateOfBirth;
}
