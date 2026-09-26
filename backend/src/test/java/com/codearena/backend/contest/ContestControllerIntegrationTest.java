package com.codearena.backend.contest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.codearena.backend.TestcontainersConfiguration;
import com.codearena.backend.security.JwtService;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ContestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String creatorToken;
    private String otherUserToken;
    private Contest futureContest;

    @BeforeEach
    void setUp() {
        contestRepository.deleteAll();
        userRepository.deleteAll();

        User creator = userRepository.save(User.builder()
                .firstname("Ada").lastname("Lovelace").email("creator@example.com")
                .passwordHash("unused").role(Role.ADMIN).build());
        User other = userRepository.save(User.builder()
                .firstname("Grace").lastname("Hopper").email("other@example.com")
                .passwordHash("unused").role(Role.USER).build());
        creatorToken = jwtService.generateToken(creator);
        otherUserToken = jwtService.generateToken(other);

        futureContest = contestRepository.save(Contest.builder()
                .contestTitle("Future Contest")
                .createdBy(creator.getId())
                .problems(List.of(ContestProblem.builder().problemId("p-1").points(10).build()))
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .build());
    }

    @Test
    void listAll_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/contests")).andExpect(status().isUnauthorized());
    }

    @Test
    void listAll_returnsContestSummaries() throws Exception {
        mockMvc.perform(get("/api/contests").header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].contestTitle", is("Future Contest")));
    }

    @Test
    void getById_allowsCreator_beforeStart() throws Exception {
        mockMvc.perform(get("/api/contests/" + futureContest.getId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCreator", is(true)));
    }

    @Test
    void getById_forbidsNonCreator_beforeStart() throws Exception {
        mockMvc.perform(get("/api/contests/" + futureContest.getId())
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/contests/does-not-exist")
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void leaderboard_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/contests/" + futureContest.getId() + "/leaderboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void leaderboard_returnsEmptyList_whenNoOneHasSubmittedYet() throws Exception {
        mockMvc.perform(get("/api/contests/" + futureContest.getId() + "/leaderboard")
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
