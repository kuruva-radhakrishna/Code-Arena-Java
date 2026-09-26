package com.codearena.backend.contest.dto;

import java.time.Instant;
import java.util.List;

import com.codearena.backend.discussion.DiscussionResponse;

public record ContestDetailResponse(
        String id,
        String contestTitle,
        String createdBy,
        String description,
        Instant startTime,
        Instant endTime,
        boolean isCreator,
        List<ContestProblemResponse> problems,
        List<LeaderboardEntryResponse> leaderBoard,
        List<DiscussionResponse> discussions) {
}
