package com.codearena.backend.contest.dto;

import java.time.Instant;

import com.codearena.backend.contest.Contest;

public record ContestSummaryResponse(
        String id,
        String contestTitle,
        String description,
        Instant startTime,
        Instant endTime,
        int problemCount) {

    public static ContestSummaryResponse from(Contest contest) {
        return new ContestSummaryResponse(
                contest.getId(), contest.getContestTitle(), contest.getDescription(),
                contest.getStartTime(), contest.getEndTime(), contest.getProblems().size());
    }
}
