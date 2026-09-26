package com.codearena.backend.problem;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.codearena.backend.TestcontainersConfiguration;
import com.codearena.backend.security.JwtService;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProblemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private Problem problem;

    @BeforeEach
    void setUp() {
        problemRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .firstname("Ada").lastname("Lovelace").email("ada.problems@example.com")
                .passwordHash("unused").role(Role.USER).build());
        userToken = jwtService.generateToken(user);

        problem = problemRepository.save(Problem.builder()
                .problemName("Two Sum")
                .description("Find two numbers that add up to target")
                .difficulty(Difficulty.EASY)
                .topics(List.of("array", "hash table"))
                .testCases(List.of(
                        TestCase.builder().input("in-public").output("out-public").isPublic(true).build(),
                        TestCase.builder().input("in-hidden").output("out-hidden").isPublic(false).build()))
                .createdBy(user.getId())
                .build());
    }

    @Test
    void listAll_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/problems"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAll_returnsSummaries_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/problems").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].problemName", is("Two Sum")));
    }

    @Test
    void getById_isPublic_andHidesNonPublicTestCases() throws Exception {
        mockMvc.perform(get("/api/problems/" + problem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problemName", is("Two Sum")))
                .andExpect(jsonPath("$.publicTestCases", hasSize(1)))
                .andExpect(jsonPath("$.publicTestCases[0].input", is("in-public")));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/problems/does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addDiscussion_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/problems/" + problem.getId() + "/discussions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment":"nice problem"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addDiscussion_thenListDiscussions_roundTrips() throws Exception {
        mockMvc.perform(post("/api/problems/" + problem.getId() + "/discussions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment":"nice problem"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment", is("nice problem")))
                .andExpect(jsonPath("$.user.email", is("ada.problems@example.com")));

        mockMvc.perform(get("/api/problems/" + problem.getId() + "/discussions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].comment", is("nice problem")));
    }

    @Test
    void addDiscussion_withBlankComment_returns400() throws Exception {
        mockMvc.perform(post("/api/problems/" + problem.getId() + "/discussions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}
