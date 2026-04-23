package com.izertis.football.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for POST /login.
 */
@Data
@Schema(description = "Login credentials")
public class LoginRequest {

    @NotBlank(message = "username is required")
    @Schema(description = "Club email used at registration", example = "info@townfc.com")
    private String username;

    @NotBlank(message = "password is required")
    @Schema(description = "Club password", example = "iTk19!n.")
    private String password;
}
