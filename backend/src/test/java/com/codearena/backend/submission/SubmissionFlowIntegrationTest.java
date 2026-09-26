package com.codearena.backend.submission;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.codearena.backend.TestcontainersConfiguration;
import com.codearena.backend.compiler.CompilerClient;
import com.codearena.backend.compiler.ExecuteResponse;
import com.codearena.backend.compiler.ExecutionStatus;
import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.problem.TestCase;
import com.codearena.backend.security.JwtService;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SubmissionFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompilerClient compilerClient;

    private String userToken;
    private Problem problem;

    @BeforeEach
    void setUp() {
        contestRepository.deleteAll();
        problemRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .firstname("Ada").lastname("Lovelace").email("ada.submissions@example.com")
                .passwordHash("unused").role(Role.USER).build());
        userToken = jwtService.generateToken(user);

        problem = problemRepository.save(Problem.builder()
                .problemName("Add Two Numbers")
                .description("Return a + b")
                .difficulty(Difficulty.EASY)
                .testCases(List.of(TestCase.builder().input("1 2").output("3").isPublic(true).build()))
                .createdBy(user.getId())
                .build());
    }

    @Test
    void execute_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"PYTHON","code":"print(1)","input":""}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void execute_delegatesToCompilerClient_andDoesNotPersistAnything() throws Exception {
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "1\n", null, 5));

        mockMvc.perform(post("/api/execute")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"PYTHON","code":"print(1)","input":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.output", is("1\n")));
    }

    @Test
    void submit_acceptedFlow_thenAppearsInMySubmissions_thenViewableById() throws Exception {
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "3\n", null, 5));

        String submitBody = mockMvc.perform(post("/api/problems/" + problem.getId() + "/submissions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"CPP","code":"int main(){return 0;}"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verdict", is("ACCEPTED")))
                .andReturn().getResponse().getContentAsString();

        String submissionId = objectMapper.readTree(submitBody).get("submissionId").asText();

        mockMvc.perform(get("/api/problems/" + problem.getId() + "/submissions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verdict", is("ACCEPTED")));

        mockMvc.perform(get("/api/submissions").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].problemName", is("Add Two Numbers")));

        mockMvc.perform(get("/api/submissions/" + submissionId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict", is("ACCEPTED")));
    }

    @Test
    void submit_wrongAnswer_returnsFailedCaseForPublicTestCase() throws Exception {
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "4\n", null, 5));

        mockMvc.perform(post("/api/problems/" + problem.getId() + "/submissions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"CPP","code":"int main(){return 0;}"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verdict", is("WRONG_ANSWER")))
                .andExpect(jsonPath("$.failedCase.expectedOutput", is("3")))
                .andExpect(jsonPath("$.failedCase.actualOutput", is("4\n")));
    }

    @Test
    void contestSubmission_awardsPointsAndUpdatesLeaderboard() throws Exception {
        Contest contest = contestRepository.save(Contest.builder()
                .contestTitle("Live Now")
                .createdBy(problem.getCreatedBy())
                .problems(List.of(ContestProblem.builder().problemId(problem.getId()).points(15).build()))
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "3\n", null, 5));

        mockMvc.perform(post("/api/contests/" + contest.getId() + "/problems/" + problem.getId() + "/submissions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"CPP","code":"int main(){return 0;}"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pointsAwarded", is(15)))
                .andExpect(jsonPath("$.totalPoints", is(15)));

        mockMvc.perform(get("/api/contests/" + contest.getId() + "/leaderboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalPoints", is(15)))
                .andExpect(jsonPath("$[0].rank", is(1)));
    }

    @Test
    void contestSubmission_rejectedWhenContestNotYetStarted() throws Exception {
        Contest contest = contestRepository.save(Contest.builder()
                .contestTitle("Future Contest")
                .createdBy(problem.getCreatedBy())
                .problems(List.of(ContestProblem.builder().problemId(problem.getId()).points(15).build()))
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build());

        mockMvc.perform(post("/api/contests/" + contest.getId() + "/problems/" + problem.getId() + "/submissions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"CPP","code":"int main(){return 0;}"}
                                """))
                .andExpect(status().isForbidden());
    }
}
