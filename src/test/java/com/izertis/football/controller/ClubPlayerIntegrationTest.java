package com.izertis.football.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izertis.football.dto.request.ClubRegistrationRequest;
import com.izertis.football.dto.request.LoginRequest;
import com.izertis.football.dto.request.PlayerRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration tests covering the main API flows.
 * Uses H2 in-memory database via the 'test' Spring profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Club & Player API integration tests")
class ClubPlayerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Shared state across ordered tests
    static String tokenClubA;
    static String tokenClubB;
    static UUID clubAId;
    static UUID clubBId;
    static UUID playerId;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ClubRegistrationRequest buildClubRequest(String username, boolean publicVisible) {
        ClubRegistrationRequest req = new ClubRegistrationRequest();
        req.setUsername(username);
        req.setPassword("Password1!");
        req.setOfficialName("Official " + username);
        req.setPopularName("Popular " + username);
        req.setFederation("UEFA");
        req.setPublicVisible(publicVisible);
        return req;
    }

    private String loginAndGetToken(String username) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword("Password1!");

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    // -------------------------------------------------------------------------
    // Club registration
    // -------------------------------------------------------------------------

    @Test @Order(1)
    @DisplayName("POST /club – register Club A (public)")
    void registerClubA() throws Exception {
        MvcResult result = mockMvc.perform(post("/club")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildClubRequest("cluba@test.com", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.officialName").value("Official cluba@test.com"))
                .andReturn();

        clubAId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());
    }

    @Test @Order(2)
    @DisplayName("POST /club – register Club B (private)")
    void registerClubB() throws Exception {
        MvcResult result = mockMvc.perform(post("/club")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildClubRequest("clubb@test.com", false))))
                .andExpect(status().isCreated())
                .andReturn();

        clubBId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());
    }

    @Test @Order(3)
    @DisplayName("POST /club – duplicate username returns 409")
    void registerDuplicateUsername() throws Exception {
        mockMvc.perform(post("/club")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildClubRequest("cluba@test.com", true))))
                .andExpect(status().isConflict());
    }

    @Test @Order(4)
    @DisplayName("POST /club – invalid email returns 400")
    void registerInvalidEmail() throws Exception {
        ClubRegistrationRequest req = buildClubRequest("not-an-email", true);
        mockMvc.perform(post("/club")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(5)
    @DisplayName("POST /club – short password returns 400")
    void registerShortPassword() throws Exception {
        ClubRegistrationRequest req = buildClubRequest("valid@test.com", true);
        req.setPassword("short");
        mockMvc.perform(post("/club")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    @Test @Order(6)
    @DisplayName("POST /login – returns JWT for valid credentials")
    void loginClubA() throws Exception {
        tokenClubA = loginAndGetToken("cluba@test.com");
        Assertions.assertThat(tokenClubA).isNotBlank();
    }

    @Test @Order(7)
    @DisplayName("POST /login – returns JWT for Club B")
    void loginClubB() throws Exception {
        tokenClubB = loginAndGetToken("clubb@test.com");
        Assertions.assertThat(tokenClubB).isNotBlank();
    }

    @Test @Order(8)
    @DisplayName("POST /login – wrong password returns 401")
    void loginWrongPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("cluba@test.com");
        req.setPassword("wrongpassword");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Club listing
    // -------------------------------------------------------------------------

    @Test @Order(9)
    @DisplayName("GET /club – requires authentication")
    void listClubs_requiresAuth() throws Exception {
        mockMvc.perform(get("/club")).andExpect(status().isUnauthorized());
    }

    @Test @Order(10)
    @DisplayName("GET /club – returns only public clubs")
    void listClubs_onlyPublic() throws Exception {
        mockMvc.perform(get("/club")
                        .header("Authorization", "Bearer " + tokenClubA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].officialName",
                        hasItem("Official cluba@test.com")))
                .andExpect(jsonPath("$[*].officialName",
                        not(hasItem("Official clubb@test.com"))));
    }

    // -------------------------------------------------------------------------
    // Club detail
    // -------------------------------------------------------------------------

    @Test @Order(11)
    @DisplayName("GET /club/{id} – Club A (public) visible to Club B")
    void getClubDetail_publicClub_visibleToOther() throws Exception {
        mockMvc.perform(get("/club/" + clubAId)
                        .header("Authorization", "Bearer " + tokenClubB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerCount").value(0));
    }

    @Test @Order(12)
    @DisplayName("GET /club/{id} – Club B (private) forbidden for Club A")
    void getClubDetail_privateClub_forbiddenForOther() throws Exception {
        mockMvc.perform(get("/club/" + clubBId)
                        .header("Authorization", "Bearer " + tokenClubA))
                .andExpect(status().isForbidden());
    }

    @Test @Order(13)
    @DisplayName("GET /club/{id} – Club B visible to itself")
    void getClubDetail_privateClub_visibleToOwner() throws Exception {
        mockMvc.perform(get("/club/" + clubBId)
                        .header("Authorization", "Bearer " + tokenClubB))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // Player management
    // -------------------------------------------------------------------------

    @Test @Order(14)
    @DisplayName("POST /club/{id}/player – Club A creates a player")
    void createPlayer() throws Exception {
        PlayerRequest req = new PlayerRequest();
        req.setGivenName("Lionel");
        req.setFamilyName("Messi");
        req.setNationality("Argentine");
        req.setEmail("messi@cluba.com");
        req.setDateOfBirth(LocalDate.of(1987, 6, 24));

        MvcResult result = mockMvc.perform(post("/club/" + clubAId + "/player")
                        .header("Authorization", "Bearer " + tokenClubA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.givenName").value("Lionel"))
                .andReturn();

        playerId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());
    }

    @Test @Order(15)
    @DisplayName("POST /club/{id}/player – duplicate email returns 409")
    void createPlayer_duplicateEmail() throws Exception {
        PlayerRequest req = new PlayerRequest();
        req.setGivenName("Clone");
        req.setFamilyName("Messi");
        req.setNationality("Argentine");
        req.setEmail("messi@cluba.com");
        req.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/club/" + clubAId + "/player")
                        .header("Authorization", "Bearer " + tokenClubA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test @Order(16)
    @DisplayName("POST /club/{id}/player – Club B cannot add players to Club A")
    void createPlayer_forbiddenForNonOwner() throws Exception {
        PlayerRequest req = new PlayerRequest();
        req.setGivenName("Intruder");
        req.setFamilyName("X");
        req.setNationality("Unknown");
        req.setEmail("intruder@test.com");
        req.setDateOfBirth(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/club/" + clubAId + "/player")
                        .header("Authorization", "Bearer " + tokenClubB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test @Order(17)
    @DisplayName("GET /club/{id}/player – list returns only id, givenName, familyName")
    void listPlayers_summaryFields() throws Exception {
        mockMvc.perform(get("/club/" + clubAId + "/player")
                        .header("Authorization", "Bearer " + tokenClubA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isString())
                .andExpect(jsonPath("$[0].givenName").value("Lionel"))
                .andExpect(jsonPath("$[0].familyName").value("Messi"))
                .andExpect(jsonPath("$[0].email").doesNotExist());
    }

    @Test @Order(18)
    @DisplayName("GET /club/{id}/player/{pid} – full player detail")
    void getPlayerDetail() throws Exception {
        mockMvc.perform(get("/club/" + clubAId + "/player/" + playerId)
                        .header("Authorization", "Bearer " + tokenClubA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("messi@cluba.com"))
                .andExpect(jsonPath("$.dateOfBirth").value("1987-06-24"))
                .andExpect(jsonPath("$.nationality").value("Argentine"));
    }

    @Test @Order(19)
    @DisplayName("GET /club/{id} – player count is updated after creation")
    void clubDetail_playerCountUpdated() throws Exception {
        mockMvc.perform(get("/club/" + clubAId)
                        .header("Authorization", "Bearer " + tokenClubA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerCount").value(1));
    }

    @Test @Order(20)
    @DisplayName("PUT /club/{id}/player/{pid} – update player")
    void updatePlayer() throws Exception {
        PlayerRequest req = new PlayerRequest();
        req.setGivenName("Leo");
        req.setFamilyName("Messi");
        req.setNationality("Spanish");
        req.setEmail("messi@cluba.com");
        req.setDateOfBirth(LocalDate.of(1987, 6, 24));

        mockMvc.perform(put("/club/" + clubAId + "/player/" + playerId)
                        .header("Authorization", "Bearer " + tokenClubA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.givenName").value("Leo"))
                .andExpect(jsonPath("$.nationality").value("Spanish"));
    }

    @Test @Order(21)
    @DisplayName("DELETE /club/{id}/player/{pid} – deletes player")
    void deletePlayer() throws Exception {
        mockMvc.perform(delete("/club/" + clubAId + "/player/" + playerId)
                        .header("Authorization", "Bearer " + tokenClubA))
                .andExpect(status().isNoContent());
    }

    @Test @Order(22)
    @DisplayName("GET /club/{id}/player/{pid} – 404 after deletion")
    void getPlayerDetail_notFound_afterDeletion() throws Exception {
        mockMvc.perform(get("/club/" + clubAId + "/player/" + playerId)
                        .header("Authorization", "Bearer " + tokenClubA))
                .andExpect(status().isNotFound());
    }
}
