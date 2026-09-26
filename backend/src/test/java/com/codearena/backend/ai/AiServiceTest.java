package com.codearena.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codearena.backend.ai.dto.ChatTurn;
import com.codearena.backend.ai.dto.ProblemDraftRequest;
import com.codearena.backend.ai.dto.ProblemDraftResponse;
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemService;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private ProblemService problemService;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(geminiClient, problemService, JsonMapper.builder().build());
    }

    @Test
    void reviewCode_includesProblemContextInThePrompt() {
        Problem problem = Problem.builder().id("p-1").problemName("Two Sum")
                .description("Find two numbers that add up to target").build();
        when(problemService.findProblemOrThrow("p-1")).thenReturn(problem);
        when(geminiClient.generateContent(any())).thenReturn("Looks good!");

        String review = aiService.reviewCode("int main(){}", "p-1");

        assertThat(review).isEqualTo("Looks good!");
        verify(geminiClient).generateContent(contains("Two Sum"));
    }

    @Test
    void debugCode_includesProblemDescription_whenProvided() {
        when(geminiClient.generateContent(any())).thenReturn("You have an off-by-one error.");

        String debug = aiService.debugCode("for(int i=0;i<=n;i++)", "Sum numbers 0..n-1");

        assertThat(debug).isEqualTo("You have an off-by-one error.");
        verify(geminiClient).generateContent(contains("Sum numbers 0..n-1"));
    }

    @Test
    void debugCode_worksWithoutProblemDescription() {
        when(geminiClient.generateContent(any())).thenReturn("Fine.");

        String debug = aiService.debugCode("print(1)", null);

        assertThat(debug).isEqualTo("Fine.");
    }

    @Test
    void chat_includesRecentHistoryAndLatestMessage() {
        when(geminiClient.generateContent(any())).thenReturn("Use a hash map.");
        List<ChatTurn> history = List.of(
                new ChatTurn("user", "How do I solve two sum?"),
                new ChatTurn("assistant", "Think about complements."));

        String response = aiService.chat("Any hints on complexity?", history);

        assertThat(response).isEqualTo("Use a hash map.");
        verify(geminiClient).generateContent(contains("Any hints on complexity?"));
    }

    @Test
    void generateContestDescription_includesTitleAndProblemNames() {
        when(geminiClient.generateContent(any())).thenReturn("A fun contest!");

        String description = aiService.generateContestDescription("Weekly 1", List.of("Two Sum", "Reverse String"));

        assertThat(description).isEqualTo("A fun contest!");
        verify(geminiClient).generateContent(contains("Weekly 1"));
    }

    @Test
    void generateProblemDraft_parsesJsonResponse() {
        when(geminiClient.generateContent(any())).thenReturn("""
                Here you go:
                ```json
                {
                  "problemName": "Two Sum",
                  "description": "Find two numbers that add up to target",
                  "constraints": ["1 <= n <= 1000"],
                  "testCases": [{"input": "1 2", "output": "3", "isPublic": true}],
                  "difficulty": "EASY",
                  "topics": ["array", "hash table"],
                  "hints": ["Use a hash map"]
                }
                ```
                """);

        ProblemDraftResponse draft = aiService.generateProblemDraft(
                new ProblemDraftRequest("Two Sum", null, null, null));

        assertThat(draft.problemName()).isEqualTo("Two Sum");
        assertThat(draft.difficulty()).isEqualTo(Difficulty.EASY);
        assertThat(draft.testCases()).hasSize(1);
        assertThat(draft.testCases().get(0).output()).isEqualTo("3");
    }

    @Test
    void generateProblemDraft_throws_whenGeminiReturnsUnparsableText() {
        when(geminiClient.generateContent(any())).thenReturn("Sorry, I can't help with that.");

        assertThatThrownBy(() -> aiService.generateProblemDraft(new ProblemDraftRequest("Two Sum", null, null, null)))
                .isInstanceOf(GeminiClientException.class);
    }
}
