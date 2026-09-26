package com.codearena.backend.submission.dto;

import java.time.Instant;

import com.codearena.backend.submission.Language;
import com.codearena.backend.submission.Verdict;

public record SubmissionSummaryResponse(
        String id,
        String problemId,
        String problemName,
        Language language,
        Verdict verdict,
        Instant submittedAt,
        boolean isInContest,
        String contestId) {
}
