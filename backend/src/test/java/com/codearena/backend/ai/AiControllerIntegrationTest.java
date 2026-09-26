package com.codearena.backend.ai;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.security.JwtService;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private GeminiClient geminiClient;

    private String userToken;
    private String adminToken;
    private Problem problem;

    @BeforeEach
    void setUp() {
        problemRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .firstname("Ada").lastname("Lovelace").email("ada.ai@example.com")
                .passwordHash("unused").role(Role.USER).build());
        User admin = userRepository.save(User.builder()
                .firstname("Grace").lastname("Hopper").email("grace.ai@example.com")
                .passwordHash("unused").role(Role.ADMIN).build());
        userToken = jwtService.generateToken(user);
        adminToken = jwtService.generateToken(admin);

        problem = problemRepository.save(Problem.builder()
                .problemName("Two Sum").description("Find two numbers that add up to target")
                .difficulty(Difficulty.EASY).createdBy(admin.getId()).build());
    }

    @Test
    void review_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/ai/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"int main(){}","problemId":"%s"}
                                """.formatted(problem.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void review_returnsGeminiText_forAnyAuthenticatedUser() throws Exception {
        when(geminiClient.generateContent(any())).thenReturn("Looks solid.");

        mockMvc.perform(post("/api/ai/review")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"int main(){}","problemId":"%s"}
                                """.formatted(problem.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review", is("Looks solid.")));
    }

    @Test
    void debug_returnsGeminiText() throws Exception {
        when(geminiClient.generateContent(any())).thenReturn("Off-by-one on line 3.");

        mockMvc.perform(post("/api/ai/debug")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"for(int i=0;i<=n;i++)"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debug", is("Off-by-one on line 3.")));
    }

    @Test
    void chat_returnsGeminiText() throws Exception {
        when(geminiClient.generateContent(any())).thenReturn("Try a hash map.");

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Any hints?","chatHistory":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", is("Try a hash map.")));
    }

    @Test
    void contestDescription_requiresAdmin() throws Exception {
        mockMvc.perform(post("/api/ai/contest-description")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contestTitle":"Weekly 1","problemNames":["Two Sum"]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void contestDescription_allowedForAdmin() throws Exception {
        when(geminiClient.generateContent(any())).thenReturn("A fun weekly contest!");

        mockMvc.perform(post("/api/ai/contest-description")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contestTitle":"Weekly 1","problemNames":["Two Sum"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("A fun weekly contest!")));
    }

    @Test
    void problemDraft_requiresAdmin() throws Exception {
        mockMvc.perform(post("/api/ai/problem-draft")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"problemName":"Two Sum"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void problemDraft_allowedForAdmin_parsesGeminiJson() throws Exception {
        when(geminiClient.generateContent(any())).thenReturn("""
                {
                  "problemName": "Two Sum",
                  "description": "Find two numbers that add up to target",
                  "constraints": ["1 <= n <= 1000"],
                  "testCases": [{"input": "1 2", "output": "3", "isPublic": true}],
                  "difficulty": "EASY",
                  "topics": ["array"],
                  "hints": ["Use a hash map"]
                }
                """);

        mockMvc.perform(post("/api/ai/problem-draft")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"problemName":"Two Sum"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty", is("EASY")))
                .andExpect(jsonPath("$.testCases[0].output", is("3")));
    }

    @Test
    void review_returns502_whenGeminiUnavailable() throws Exception {
        when(geminiClient.generateContent(any())).thenThrow(new GeminiClientException("down"));

        mockMvc.perform(post("/api/ai/review")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"int main(){}","problemId":"%s"}
                                """.formatted(problem.getId())))
                .andExpect(status().isBadGateway());
    }
}
