package com.codearena.backend.legacydata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.codearena.backend.contest.Contest;

class LegacyContestReadConverterTest {

    private final LegacyContestReadConverter converter = new LegacyContestReadConverter();

    @Test
    void convert_readsLegacyProblemRefsAndDiscussionsWithoutThrowing() {
        ObjectId creatorId = new ObjectId();
        ObjectId problemId = new ObjectId();
        Document legacy = new Document()
                .append("_id", new ObjectId())
                .append("contestTitle", "Weekly Contest 1")
                .append("createdBy", creatorId)
                .append("problems", List.of(
                        new Document("problem_id", problemId).append("points", 10)))
                .append("startTime", Date.from(java.time.Instant.parse("2025-01-01T00:00:00Z")))
                .append("endTime", Date.from(java.time.Instant.parse("2025-01-01T02:00:00Z")))
                .append("description", "First contest")
                .append("Discussions", List.of(
                        new Document("user", creatorId).append("comment", "Good luck")))
                // Old-style leaderboard shape this app has no equivalent for.
                .append("leaderBoard", List.of(
                        new Document("user_id", creatorId).append("submissions", List.of(List.of()))));

        Contest contest = converter.convert(legacy);

        assertThat(contest.getContestTitle()).isEqualTo("Weekly Contest 1");
        assertThat(contest.getCreatedBy()).isEqualTo(creatorId.toHexString());
        assertThat(contest.getProblems()).hasSize(1);
        assertThat(contest.getProblems().get(0).getProblemId()).isEqualTo(problemId.toHexString());
        assertThat(contest.getProblems().get(0).getPoints()).isEqualTo(10);
        assertThat(contest.getDiscussions()).hasSize(1);
        assertThat(contest.getDiscussions().get(0).getComment()).isEqualTo("Good luck");
        // The old shape's per-problem submission history has no equivalent field
        // here, so the entry still exists (its userId is readable) but with an
        // empty per-problem history - not silently dropped, and not a throw.
        assertThat(contest.getLeaderBoard()).hasSize(1);
        assertThat(contest.getLeaderBoard().get(0).getUserId()).isEqualTo(creatorId.toHexString());
        assertThat(contest.getLeaderBoard().get(0).getPerProblemPoints()).isEmpty();
    }

    @Test
    void convert_readsACurrentAppShapedDocumentToo() {
        ObjectId problemId = new ObjectId();
        ObjectId participantId = new ObjectId();
        Document current = new Document()
                .append("_id", new ObjectId())
                .append("contestTitle", "New Contest")
                .append("createdBy", "abc123")
                .append("problems", List.of(
                        new Document("problemId", problemId.toHexString()).append("points", 5)))
                .append("startTime", Date.from(java.time.Instant.parse("2025-02-01T00:00:00Z")))
                .append("endTime", Date.from(java.time.Instant.parse("2025-02-01T02:00:00Z")))
                .append("leaderBoard", List.of(
                        new Document("userId", participantId.toHexString())
                                .append("perProblemPoints", List.of(10, 0))
                                .append("perProblemSolvedAt", java.util.Arrays.asList(
                                        Date.from(java.time.Instant.parse("2025-02-01T01:00:00Z")), null))
                                .append("lastSubmissionAt", Date.from(java.time.Instant.parse("2025-02-01T01:00:00Z")))));

        Contest contest = converter.convert(current);

        assertThat(contest.getCreatedBy()).isEqualTo("abc123");
        assertThat(contest.getProblems().get(0).getProblemId()).isEqualTo(problemId.toHexString());
        // This is the case that actually matters day-to-day: a contest this app
        // itself created and updated must read back exactly as written.
        assertThat(contest.getLeaderBoard()).hasSize(1);
        var entry = contest.getLeaderBoard().get(0);
        assertThat(entry.getUserId()).isEqualTo(participantId.toHexString());
        assertThat(entry.getPerProblemPoints()).containsExactly(10, 0);
        assertThat(entry.getLastSubmissionAt()).isEqualTo(java.time.Instant.parse("2025-02-01T01:00:00Z"));
    }
}
