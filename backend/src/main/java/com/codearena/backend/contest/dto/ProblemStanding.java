package com.codearena.backend.contest.dto;

import java.time.Instant;

public record ProblemStanding(String problemId, int points, Instant solvedAt) {
}
