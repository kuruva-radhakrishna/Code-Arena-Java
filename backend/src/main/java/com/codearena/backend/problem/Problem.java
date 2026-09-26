package com.codearena.backend.problem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "problems")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Problem {

    @Id
    private String id;

    @Indexed(unique = true)
    private String problemName;

    private String description;

    @Builder.Default
    private List<String> constraints = new ArrayList<>();

    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();

    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Builder.Default
    private List<String> topics = new ArrayList<>();

    @Builder.Default
    private List<String> hints = new ArrayList<>();

    @Builder.Default
    private List<Discussion> discussions = new ArrayList<>();

    @Builder.Default
    private int likes = 0;

    @Builder.Default
    private int dislikes = 0;

    private String createdBy;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
