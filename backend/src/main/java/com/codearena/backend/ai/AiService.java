package com.codearena.backend.ai;

import java.util.List;

import org.springframework.stereotype.Service;

import com.codearena.backend.ai.dto.ChatTurn;
import com.codearena.backend.ai.dto.ProblemDraftRequest;
import com.codearena.backend.ai.dto.ProblemDraftResponse;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemService;
import com.codearena.backend.problem.Topics;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiService {

    private static final int MAX_CHAT_HISTORY_TURNS = 10;

    private final GeminiClient geminiClient;
    private final ProblemService problemService;
    private final ObjectMapper objectMapper;

    public AiService(GeminiClient geminiClient, ProblemService problemService, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.problemService = problemService;
        this.objectMapper = objectMapper;
    }

    public String reviewCode(String code, String problemId) {
        Problem problem = problemService.findProblemOrThrow(problemId);
        String prompt = """
                You are reviewing a user's solution to a coding problem. Respond in Markdown.

                Problem: %s
                %s

                Review the following code for correctness, edge cases, readability, and performance.
                Give concrete, actionable suggestions.

                ```
                %s
                ```
                """.formatted(problem.getProblemName(), problem.getDescription(), code);
        return geminiClient.generateContent(prompt);
    }

    public String debugCode(String code, String problemDescription) {
        String context = (problemDescription == null || problemDescription.isBlank())
                ? "" : "Problem context: " + problemDescription + "\n\n";
        String prompt = """
                You are helping debug a piece of code. Respond concisely in Markdown, focusing on what is
                wrong and how to fix it - do not restate the whole problem.

                %s```
                %s
                ```
                """.formatted(context, code);
        return geminiClient.generateContent(prompt);
    }

    public String chat(String message, List<ChatTurn> history) {
        StringBuilder prompt = new StringBuilder("""
                You are a helpful programming assistant embedded in a competitive-programming and coding
                practice platform called CodeArena. Answer programming, algorithm, and interview-prep
                questions clearly and concisely.

                """);

        if (history != null) {
            history.stream()
                    .skip(Math.max(0, history.size() - MAX_CHAT_HISTORY_TURNS))
                    .forEach(turn -> prompt.append(turn.role()).append(": ").append(turn.content()).append('\n'));
        }
        prompt.append("user: ").append(message);

        return geminiClient.generateContent(prompt.toString());
    }

    public String generateContestDescription(String contestTitle, List<String> problemNames) {
        String prompt = """
                Write a short, engaging description (2-4 sentences) for a coding contest.

                Contest title: %s
                Problems included: %s
                """.formatted(contestTitle, String.join(", ", problemNames));
        return geminiClient.generateContent(prompt);
    }

    public ProblemDraftResponse generateProblemDraft(ProblemDraftRequest request) {
        String prompt = """
                You are helping an admin author a coding problem for a competitive-programming platform.
                Given what they've already written, fill in the rest and respond with ONLY a single JSON
                object (no markdown fences, no commentary) matching exactly this shape:

                {
                  "problemName": string,
                  "description": string (the full problem statement),
                  "constraints": string[],
                  "testCases": [{"input": string, "output": string, "isPublic": boolean}] (at least 3, at least 1 public),
                  "difficulty": "EASY" | "MEDIUM" | "HARD",
                  "topics": string[] (chosen only from this exact list: %s),
                  "hints": string[]
                }

                Already provided by the admin:
                problemName: %s
                description: %s
                difficulty: %s
                topics: %s
                """.formatted(
                String.join(", ", Topics.ALLOWED),
                request.problemName(),
                request.description() == null ? "(not provided - invent a reasonable problem)" : request.description(),
                request.difficulty() == null ? "(not provided - choose one)" : request.difficulty(),
                request.topics() == null || request.topics().isEmpty() ? "(not provided - choose 1-3)" : request.topics());

        String raw = geminiClient.generateContent(prompt);
        try {
            return objectMapper.readValue(extractJson(raw), ProblemDraftResponse.class);
        } catch (JacksonException ex) {
            throw new GeminiClientException("Gemini did not return a valid problem draft", ex);
        }
    }

    private String extractJson(String raw) {
        String trimmed = raw.strip();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).strip();
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            throw new GeminiClientException("Gemini did not return valid JSON for the problem draft");
        }
        return trimmed.substring(start, end + 1);
    }
}
