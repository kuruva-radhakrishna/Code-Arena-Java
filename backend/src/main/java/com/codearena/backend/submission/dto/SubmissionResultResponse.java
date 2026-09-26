package com.codearena.backend.submission.dto;

import java.util.List;

import com.codearena.backend.submission.Verdict;

public record SubmissionResultResponse(
        String submissionId,
        Verdict verdict,
        List<Verdict> testCaseVerdicts,
        FailedCaseResponse failedCase,
        Long executionTimeMs) {
}
