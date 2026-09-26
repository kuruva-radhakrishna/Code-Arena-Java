package com.codearena.backend.submission.dto;

public record ContestSubmissionResultResponse(
        SubmissionResultResponse result,
        int pointsAwarded,
        int totalPoints) {
}
