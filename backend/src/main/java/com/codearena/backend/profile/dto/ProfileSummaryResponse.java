package com.codearena.backend.profile.dto;

import java.util.List;

import com.codearena.backend.contest.dto.ContestSummaryResponse;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.submission.dto.SubmissionSummaryResponse;
import com.codearena.backend.user.dto.UserResponse;

public record ProfileSummaryResponse(
        UserResponse user,
        DifficultyStats solvedStats,
        DifficultyStats problemTotals,
        List<ProblemSummaryResponse> problemsCreatedByMe,
        List<SubmissionSummaryResponse> recentSubmissions,
        List<ContestSummaryResponse> contestsCreatedByMe,
        List<AttendedContestResponse> attendedContests) {
}
