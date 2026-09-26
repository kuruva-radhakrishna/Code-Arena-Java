package com.codearena.backend.contest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A participant's standing in a contest. Populated/updated by the submissions
 * flow (Stage 4) as they submit to contest problems.
 *
 * Unlike the original app, which duplicated each full submission into a 2D
 * array on the leaderboard entry, this only keeps the per-problem points
 * awarded and when each problem was first solved (plus the timestamp of the
 * most recent submission, for tie-breaking) — enough to render and sort the
 * leaderboard without re-embedding submission history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {

    private String userId;

    /** Parallel to the contest's problem list; points awarded per problem (0 if not solved yet). */
    @Builder.Default
    private List<Integer> perProblemPoints = new ArrayList<>();

    /** Parallel to the contest's problem list; when each problem was first accepted, or null. */
    @Builder.Default
    private List<Instant> perProblemSolvedAt = new ArrayList<>();

    /** Timestamp of this user's most recent submission to the contest, any verdict. */
    private Instant lastSubmissionAt;

    public int totalPoints() {
        return perProblemPoints.stream().mapToInt(Integer::intValue).sum();
    }
}
