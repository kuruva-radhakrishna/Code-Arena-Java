package com.codearena.backend.submission;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    private String id;

    private String userId;

    private String problemId;

    private String contestId;

    private Language language;

    private String code;

    private Verdict verdict;

    private Long executionTimeMs;

    @Builder.Default
    private boolean isInContest = false;

    @Builder.Default
    private Instant submittedAt = Instant.now();
}
