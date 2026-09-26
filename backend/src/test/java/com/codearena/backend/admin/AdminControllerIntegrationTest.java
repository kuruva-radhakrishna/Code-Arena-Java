package com.codearena.backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.codearena.backend.TestcontainersConfiguration;
import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.problem.TestCase;
import com.codearena.backend.security.JwtService;
import com.codearena.backend.submission.Submission;
import com.codearena.backend.submission.SubmissionRepository;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String otherAdminToken;
    private String userToken;
    private String adminId;

    @BeforeEach
    void setUp() {
        submissionRepository.deleteAll();
        contestRepository.deleteAll();
        problemRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .firstname("Ada").lastname("Lovelace").email("admin@example.com")
                .passwordHash("unused").role(Role.ADMIN).build());
        User otherAdmin = userRepository.save(User.builder()
                .firstname("Grace").lastname("Hopper").email("other-admin@example.com")
                .passwordHash("unused").role(Role.ADMIN).build());
        User user = userRepository.save(User.builder()
                .firstname("Bob").lastname("Regular").email("user@example.com")
                .passwordHash("unused").role(Role.USER).build());
        adminId = admin.getId();
        adminToken = jwtService.generateToken(admin);
        otherAdminToken = jwtService.generateToken(otherAdmin);
        userToken = jwtService.generateToken(user);
    }

    private String validProblemPayload(String name) {
        return """
                {
                  "problemName": "%s",
                  "description": "A description that is definitely long enough",
                  "constraints": ["1 <= n <= 1000"],
                  "testCases": [{"input": "1 2", "output": "3", "isPublic": true}],
                  "difficulty": "EASY",
                  "topics": ["array"],
                  "hints": []
                }
                """.formatted(name);
    }

    @Test
    void createProblem_requiresAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/problems")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProblemPayload("Two Sum")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProblem_thenListMine_thenUpdate_thenDelete() throws Exception {
        String createBody = mockMvc.perform(post("/api/admin/problems")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProblemPayload("Two Sum")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.testCases[0].isPublic", is(true)))
                .andReturn().getResponse().getContentAsString();
        String problemId = objectMapper.readTree(createBody).get("id").asText();

        mockMvc.perform(get("/api/admin/problems").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(put("/api/admin/problems/" + problemId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProblemPayload("Two Sum Updated")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problemName", is("Two Sum Updated")));

        mockMvc.perform(delete("/api/admin/problems/" + problemId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/problems").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateProblem_forbiddenForNonOwnerAdmin() throws Exception {
        Problem problem = problemRepository.save(Problem.builder()
                .problemName("Owned By First Admin").description("desc desc desc desc")
                .difficulty(Difficulty.EASY)
                .testCases(List.of(TestCase.builder().input("a").output("b").isPublic(true).build()))
                .createdBy(adminId).build());

        mockMvc.perform(put("/api/admin/problems/" + problem.getId())
                        .header("Authorization", "Bearer " + otherAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProblemPayload("Hijacked Name")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProblem_cascadesDeleteOfItsSubmissions() throws Exception {
        Problem problem = problemRepository.save(Problem.builder()
                .problemName("To Be Deleted").description("desc desc desc desc")
                .difficulty(Difficulty.EASY)
                .testCases(List.of(TestCase.builder().input("a").output("b").isPublic(true).build()))
                .createdBy(adminId).build());
        submissionRepository.save(Submission.builder().userId("someone").problemId(problem.getId()).build());

        mockMvc.perform(delete("/api/admin/problems/" + problem.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(submissionRepository.findAll()).isEmpty();
    }

    private String validContestPayload(String title, List<String> problemIds) {
        String problemsJson = problemIds.stream()
                .map(id -> "{\"problemId\":\"" + id + "\"}")
                .reduce((a, b) -> a + "," + b).orElse("");
        return """
                {
                  "contestTitle": "%s",
                  "description": "A fun contest description",
                  "startTime": "%s",
                  "endTime": "%s",
                  "problems": [%s]
                }
                """.formatted(title, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS), problemsJson);
    }

    private Problem saveProblem(String name) {
        return problemRepository.save(Problem.builder()
                .problemName(name).description("desc desc desc desc").difficulty(Difficulty.EASY)
                .testCases(List.of(TestCase.builder().input("a").output("b").isPublic(true).build()))
                .createdBy(adminId).build());
    }

    @Test
    void createContest_requiresAtLeastThreeProblems() throws Exception {
        Problem p1 = saveProblem("P1");
        Problem p2 = saveProblem("P2");

        mockMvc.perform(post("/api/admin/contests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validContestPayload("Weekly Contest", List.of(p1.getId(), p2.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createContest_thenUpdate_thenDelete_clearingSubmissionContestId() throws Exception {
        Problem p1 = saveProblem("P1");
        Problem p2 = saveProblem("P2");
        Problem p3 = saveProblem("P3");

        String createBody = mockMvc.perform(post("/api/admin/contests")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validContestPayload("Weekly Contest", List.of(p1.getId(), p2.getId(), p3.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.problemCount", is(3)))
                .andReturn().getResponse().getContentAsString();
        String contestId = objectMapper.readTree(createBody).get("id").asText();

        mockMvc.perform(put("/api/admin/contests/" + contestId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validContestPayload("Weekly Contest Renamed", List.of(p1.getId(), p2.getId(), p3.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contestTitle", is("Weekly Contest Renamed")));

        Submission submission = submissionRepository.save(
                Submission.builder().userId("someone").problemId(p1.getId()).contestId(contestId).build());

        mockMvc.perform(delete("/api/admin/contests/" + contestId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        Submission reloaded = submissionRepository.findById(submission.getId()).orElseThrow();
        assertThat(reloaded.getContestId()).isNull();
    }

    @Test
    void updateContest_forbiddenOnceContestHasStarted() throws Exception {
        Problem p1 = saveProblem("P1");
        Problem p2 = saveProblem("P2");
        Problem p3 = saveProblem("P3");
        Contest started = contestRepository.save(Contest.builder()
                .contestTitle("Already Live").description("desc desc desc")
                .createdBy(adminId)
                .problems(List.of(
                        ContestProblem.builder().problemId(p1.getId()).points(4).build(),
                        ContestProblem.builder().problemId(p2.getId()).points(4).build(),
                        ContestProblem.builder().problemId(p3.getId()).points(4).build()))
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());

        mockMvc.perform(put("/api/admin/contests/" + started.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validContestPayload("New Title", List.of(p1.getId(), p2.getId(), p3.getId()))))
                .andExpect(status().isForbidden());
    }
}
