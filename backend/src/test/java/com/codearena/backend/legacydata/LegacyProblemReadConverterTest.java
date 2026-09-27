package com.codearena.backend.legacydata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;

class LegacyProblemReadConverterTest {

    private final LegacyProblemReadConverter converter = new LegacyProblemReadConverter();

    @Test
    void convert_readsALegacyNodeAppShapedDocumentWithoutThrowing() {
        ObjectId problemId = new ObjectId();
        ObjectId creatorId = new ObjectId();
        Document legacy = new Document()
                .append("_id", problemId)
                .append("problemName", "Two Sum")
                .append("problemDescription", "Find two numbers that add up to a target.")
                .append("Constraints", List.of("1 <= n <= 1000"))
                .append("TestCases", List.of(
                        new Document("input", "1 2").append("output", "3").append("isPublic", true),
                        new Document("input", "5 5").append("output", "10").append("isPublic", false)))
                .append("difficulty", "easy")
                .append("topics", List.of("array", "hash table"))
                .append("hints", List.of("Use a hash map"))
                .append("likes", 3)
                .append("dislikes", 1)
                .append("CreatedBy", creatorId)
                .append("CreatedAt", Date.from(java.time.Instant.parse("2025-01-01T00:00:00Z")))
                .append("Discussions", List.of(
                        new Document("user", creatorId).append("comment", "Nice problem").append("likes", 2)));

        Problem problem = converter.convert(legacy);

        assertThat(problem.getId()).isEqualTo(problemId.toHexString());
        assertThat(problem.getProblemName()).isEqualTo("Two Sum");
        assertThat(problem.getDescription()).isEqualTo("Find two numbers that add up to a target.");
        assertThat(problem.getConstraints()).containsExactly("1 <= n <= 1000");
        assertThat(problem.getTestCases()).hasSize(2);
        assertThat(problem.getTestCases().get(1).isPublic()).isFalse();
        assertThat(problem.getDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(problem.getCreatedBy()).isEqualTo(creatorId.toHexString());
        assertThat(problem.getDiscussions()).hasSize(1);
        assertThat(problem.getDiscussions().get(0).getUserId()).isEqualTo(creatorId.toHexString());
        assertThat(problem.getDiscussions().get(0).getComment()).isEqualTo("Nice problem");
    }

    @Test
    void convert_fallsBackToMediumForAnUnrecognizedDifficulty() {
        Document legacy = new Document()
                .append("_id", new ObjectId())
                .append("problemName", "Mystery")
                .append("problemDescription", "...")
                .append("difficulty", "extreme");

        Problem problem = converter.convert(legacy);

        assertThat(problem.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
    }

    @Test
    void convert_readsACurrentAppShapedDocumentToo() {
        Document current = new Document()
                .append("_id", new ObjectId())
                .append("problemName", "Fresh Problem")
                .append("description", "Written by the new app")
                .append("constraints", List.of("n >= 1"))
                .append("difficulty", "HARD")
                .append("createdBy", "abc123");

        Problem problem = converter.convert(current);

        assertThat(problem.getDescription()).isEqualTo("Written by the new app");
        assertThat(problem.getDifficulty()).isEqualTo(Difficulty.HARD);
        assertThat(problem.getCreatedBy()).isEqualTo("abc123");
    }
}
