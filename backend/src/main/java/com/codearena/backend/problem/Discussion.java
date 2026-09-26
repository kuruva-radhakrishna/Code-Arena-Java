package com.codearena.backend.problem;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discussion {

    private String id;

    private String userId;

    private String comment;

    @Builder.Default
    private int likes = 0;

    @Builder.Default
    private int dislikes = 0;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
