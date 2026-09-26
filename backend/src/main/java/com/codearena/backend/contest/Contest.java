package com.codearena.backend.contest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.codearena.backend.discussion.Discussion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "contests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contest {

    @Id
    private String id;

    @Indexed(unique = true)
    private String contestTitle;

    private String createdBy;

    @Builder.Default
    private List<ContestProblem> problems = new ArrayList<>();

    private Instant startTime;

    private Instant endTime;

    @Builder.Default
    private String description = "";

    @Builder.Default
    private List<LeaderboardEntry> leaderBoard = new ArrayList<>();

    @Builder.Default
    private List<Discussion> discussions = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    public boolean hasStarted(Instant now) {
        return !now.isBefore(startTime);
    }

    public boolean isLive(Instant now) {
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
