package com.codearena.compiler.execution;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExecutionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void execute_runsPythonEndToEnd() throws Exception {
        mockMvc.perform(post("/api/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"PYTHON","code":"print(1 + 1)","input":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.output", is("2\n")));
    }

    @Test
    void execute_returns400_whenCodeMissing() throws Exception {
        mockMvc.perform(post("/api/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"PYTHON","code":"","input":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void execute_returns400_forUnrecognizedLanguage() throws Exception {
        mockMvc.perform(post("/api/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"COBOL","code":"print(1)","input":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}
