package com.codearena.backend.admin.dto;

import java.util.List;

import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;

/**
 * Full problem view for its owning admin, unlike {@code ProblemDetailResponse} -
 * includes every test case, not just the ones marked public.
 */
public record AdminProblemResponse(
        String id,
        String problemName,
        String description,
        List<String> constraints,
        List<AdminTestCaseResponse> testCases,
        Difficulty difficulty,
        List<String> topics,
        List<String> hints) {

    public static AdminProblemResponse from(Problem problem) {
        return new AdminProblemResponse(
                problem.getId(), problem.getProblemName(), problem.getDescription(), problem.getConstraints(),
                problem.getTestCases().stream().map(AdminTestCaseResponse::from).toList(),
                problem.getDifficulty(), problem.getTopics(), problem.getHints());
    }
}
