package com.codearena.backend.problem.dto;

import java.util.List;

import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;

public record ProblemSummaryResponse(
        String id,
        String problemName,
        Difficulty difficulty,
        List<String> topics,
        int likes,
        int dislikes) {

    public static ProblemSummaryResponse from(Problem problem) {
        return new ProblemSummaryResponse(
                problem.getId(),
                problem.getProblemName(),
                problem.getDifficulty(),
                problem.getTopics(),
                problem.getLikes(),
                problem.getDislikes());
    }
}
