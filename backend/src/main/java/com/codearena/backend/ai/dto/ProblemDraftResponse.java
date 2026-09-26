package com.codearena.backend.ai.dto;

import java.util.List;

import com.codearena.backend.problem.Difficulty;

public record ProblemDraftResponse(
        String problemName,
        String description,
        List<String> constraints,
        List<TestCaseDraft> testCases,
        Difficulty difficulty,
        List<String> topics,
        List<String> hints) {
}
