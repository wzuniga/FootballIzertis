package com.izertis.football.controller;

import com.izertis.football.domain.Club;
import com.izertis.football.dto.request.ClubRegistrationRequest;
import com.izertis.football.dto.request.ClubUpdateRequest;
import com.izertis.football.dto.response.ClubDetailResponse;
import com.izertis.football.dto.response.ClubSummaryResponse;
import com.izertis.football.dto.response.ErrorResponse;
import com.izertis.football.mapper.ClubMapper;
import com.izertis.football.security.CurrentClubResolver;
import com.izertis.football.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Club registration, listing, detail, and update operations.
 */
@RestController
@RequestMapping("/club")
@RequiredArgsConstructor
@Tag(name = "Clubs", description = "Club registration and management")
public class ClubController {

    private final ClubService clubService;
    private final ClubMapper clubMapper;
    private final CurrentClubResolver currentClubResolver;

    // -------------------------------------------------------------------------
    // POST /club  – Registration (public, no token required)
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Register a new Football Club")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Club registered successfully",
                    content = @Content(schema = @Schema(implementation = ClubDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClubDetailResponse> register(
            @Valid @RequestBody ClubRegistrationRequest request) {
        Club saved = clubService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clubMapper.toDetailResponse(saved));
    }

    // -------------------------------------------------------------------------
    // GET /club  – List public clubs (token required)
    // -------------------------------------------------------------------------

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all public Football Clubs",
               description = "Returns clubs with public=true. Optionally filter by name (partial, case-insensitive) and/or federation (exact, case-insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of public clubs"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ClubSummaryResponse>> listPublicClubs(
            @Parameter(description = "Filter by name (partial match on officialName or popularName)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by federation acronym (exact match, e.g. UEFA)")
            @RequestParam(required = false) String federation) {
        return ResponseEntity.ok(clubService.getPublicClubs(name, federation));
    }

    // -------------------------------------------------------------------------
    // GET /club/{clubId}  – Club detail (token required)
    // -------------------------------------------------------------------------

    @GetMapping("/{clubId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get details of a specific club",
               description = "Public clubs: visible to all authenticated users. " +
                             "Private clubs: only accessible by the owning club.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Club details including player count",
                    content = @Content(schema = @Schema(implementation = ClubDetailResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (private club)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Club not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClubDetailResponse> getClubDetail(
            @Parameter(description = "UUID of the club") @PathVariable UUID clubId) {
        UUID requestingClubId = currentClubResolver.getCurrentClubId();
        return ResponseEntity.ok(clubService.getClubDetail(clubId, requestingClubId));
    }

    // -------------------------------------------------------------------------
    // PUT /club/{clubId}  – Update club (token required, owner only)
    // -------------------------------------------------------------------------

    @PutMapping("/{clubId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a club's details",
               description = "Only the authenticated club can update its own details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Club updated successfully",
                    content = @Content(schema = @Schema(implementation = ClubDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the owner of this club",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Club not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClubDetailResponse> updateClub(
            @Parameter(description = "UUID of the club to update") @PathVariable UUID clubId,
            @Valid @RequestBody ClubUpdateRequest request) {
        UUID requestingClubId = currentClubResolver.getCurrentClubId();
        return ResponseEntity.ok(clubService.updateClub(clubId, request, requestingClubId));
    }
}
