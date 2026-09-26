package com.codearena.backend.profile.dto;

import java.time.Instant;

public record AttendedContestResponse(
        String contestId,
        String contestTitle,
        Instant startTime,
        int rank,
        int totalPoints,
        Instant lastSubmissionAt) {
}
