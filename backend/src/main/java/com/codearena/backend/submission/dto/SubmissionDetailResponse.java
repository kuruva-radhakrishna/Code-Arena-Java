package com.codearena.backend.submission.dto;

import java.time.Instant;

import com.codearena.backend.submission.Language;
import com.codearena.backend.submission.Verdict;

public record SubmissionDetailResponse(
        String id,
        String userId,
        String problemId,
        String problemName,
        String contestId,
        Language language,
        String code,
        Verdict verdict,
        Long executionTimeMs,
        Instant submittedAt,
        boolean isInContest) {
}
