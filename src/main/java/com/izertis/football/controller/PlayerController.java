package com.izertis.football.controller;

import com.izertis.football.dto.request.PlayerRequest;
import com.izertis.football.dto.response.ErrorResponse;
import com.izertis.football.dto.response.PlayerDetailResponse;
import com.izertis.football.dto.response.PlayerSummaryResponse;
import com.izertis.football.security.CurrentClubResolver;
import com.izertis.football.service.PlayerService;
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
 * REST controller for Player management within a Club.
 */
@RestController
@RequestMapping("/club/{clubId}/player")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Players", description = "Manage federated players within a club")
public class PlayerController {

    private final PlayerService playerService;
    private final CurrentClubResolver currentClubResolver;

    // -------------------------------------------------------------------------
    // POST /club/{clubId}/player  – Create player (owner only)
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Register a new player under a club",
               description = "Only the owning club can add players.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Player created",
                    content = @Content(schema = @Schema(implementation = PlayerDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the owner of this club",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Club not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Player email already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlayerDetailResponse> createPlayer(
            @Parameter(description = "UUID of the club") @PathVariable UUID clubId,
            @Valid @RequestBody PlayerRequest request) {
        UUID requestingClubId = currentClubResolver.getCurrentClubId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerService.createPlayer(clubId, request, requestingClubId));
    }

    // -------------------------------------------------------------------------
    // GET /club/{clubId}/player  – List players
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(summary = "List players of a club",
               description = "Returns id, givenName and familyName only. "
                             + "Optionally filter by name (partial) and/or nationality (exact). "
                             + "For private clubs only the owning club may access this list.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Player list"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Club not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PlayerSummaryResponse>> listPlayers(
            @Parameter(description = "UUID of the club") @PathVariable UUID clubId,
            @Parameter(description = "Filter by name (partial match on givenName or familyName)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by nationality (exact match, case-insensitive)")
            @RequestParam(required = false) String nationality) {
        UUID requestingClubId = currentClubResolver.getCurrentClubId();
        return ResponseEntity.ok(playerService.listPlayers(clubId, requestingClubId, name, nationality));
    }

    // -------------------------------------------------------------------------
    // GET /club/{clubId}/player/{playerId}  – Player detail
    // -------------------------------------------------------------------------

    @GetMapping("/{playerId}")
    @Operation(summary = "Get full details of a player",
               description = "For private clubs only the owning club may access player details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Player details",
                    content = @Content(schema = @Schema(implementation = PlayerDetailResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player or club not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlayerDetailResponse> getPlayerDetail(
            @Parameter(description = "UUID of the club") @PathVariable UUID clubId,
            @Parameter(description = "UUID of the player") @PathVariable UUID playerId) {
        UUID requestingClubId = currentClubResolver.getCurrentClubId();
        return ResponseEntity.ok(playerService.getPlayerDetail(clubId, playerId, requestingClubId));
    }

    // -------------------------------------------------------------------------
    // PUT /club/{clubId}/player/{playerId}  – Update player (owner only)
    // -------------------------------------------------------------------------

    @PutMapping("/{playerId}")
    @Operation(summary = "Update a player's details",
               description = "Only the owning club can update its players.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Player updated",
                    content = @Content(schema = @Schema(implementation = PlayerDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not the owner of this club",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player or club not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Player email already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlayerDetailResponse> updatePlayer(
            @Parameter(description = "UUID of the club") @PathVariable UUID clubId,
            @Parameter(description = "UUID of the player") @PathVariable UUID playerId,
            @Valid @RequestBody PlayerRequest request) {
        UUID requestingClubId = currentClubResolver.getCurrentClubId();
        return ResponseEntity.ok(playerService.updatePlayer(clubId, playerId, request, requestingClubId));
    }

    // -------------------------------------------------------------------------
    // DELETE /club/{clubId}/player/{playerId}  – Delete player (owner only)
    // -------------------------------------------------------------------------

    @DeleteMapping("/{playerId}")
    @Operation(summary = "Delete a player from a club",
               description = "Only the owning club can delete its players.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Player deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not the owner of this club",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player or club not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletePlayer(
            @Parameter(description = "UUID of the club") @PathVariable UUID clubId,
            @Parameter(description = "UUID of the player") @PathVariable UUID playerId) {
        UUID requestingClubId = currentClubResolver.getCurrentClubId();
        playerService.deletePlayer(clubId, playerId, requestingClubId);
        return ResponseEntity.noContent().build();
    }
}
