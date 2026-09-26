package com.codearena.backend.profile;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = userRepository.save(User.builder()
                .firstname("Ada").lastname("Lovelace").email("ada.profile@example.com")
                .passwordHash("unused").role(Role.USER).build());
        token = jwtService.generateToken(user);
    }

    @Test
    void summary_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/profile/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    void summary_returnsUserAndEmptyStatsForNewAccount() throws Exception {
        mockMvc.perform(get("/api/profile/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is("ada.profile@example.com")))
                .andExpect(jsonPath("$.solvedStats.total", is(0)))
                .andExpect(jsonPath("$.problemsCreatedByMe", hasSize(0)))
                .andExpect(jsonPath("$.attendedContests", hasSize(0)));
    }
}
