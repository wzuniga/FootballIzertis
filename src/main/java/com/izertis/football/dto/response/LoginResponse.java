package com.izertis.football.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response for POST /login – contains the JWT bearer token.
 */
@Data
@AllArgsConstructor
@Schema(description = "JWT token issued after a successful login")
public class LoginResponse {

    @Schema(description = "Bearer JWT token to be sent in the Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
}
