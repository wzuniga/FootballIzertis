package com.izertis.football.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Standard error response envelope returned on all non-2xx status codes.
 */
@Data
@AllArgsConstructor
@Schema(description = "Error response body")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Short error code", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable description of the error", example = "username must be a valid email address")
    private String message;
}
