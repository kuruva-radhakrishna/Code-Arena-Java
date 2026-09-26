package com.codearena.backend.contest.dto;

import java.time.Instant;
import java.util.List;

import com.codearena.backend.user.dto.UserSummary;

public record LeaderboardEntryResponse(
        int rank,
        UserSummary user,
        int totalPoints,
        Instant lastSubmissionAt,
        List<ProblemStanding> problems) {
}
