package com.codearena.backend.problem.dto;

import java.util.List;

import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.TestCase;

/**
 * Public view of a problem. Only public test cases are included — hidden
 * (non-public) test cases are never exposed over the API, unlike the original
 * app, which returned every test case (including hidden ones) to any caller.
 */
public record ProblemDetailResponse(
        String id,
        String problemName,
        String description,
        List<String> constraints,
        List<TestCaseResponse> publicTestCases,
        Difficulty difficulty,
        List<String> topics,
        List<String> hints,
        int likes,
        int dislikes,
        String createdBy) {

    public static ProblemDetailResponse from(Problem problem) {
        List<TestCaseResponse> publicTestCases = problem.getTestCases().stream()
                .filter(TestCase::isPublic)
                .map(TestCaseResponse::from)
                .toList();

        return new ProblemDetailResponse(
                problem.getId(),
                problem.getProblemName(),
                problem.getDescription(),
                problem.getConstraints(),
                publicTestCases,
                problem.getDifficulty(),
                problem.getTopics(),
                problem.getHints(),
                problem.getLikes(),
                problem.getDislikes(),
                problem.getCreatedBy());
    }
}
