package com.codearena.backend.profile.dto;

public record DifficultyStats(int easy, int medium, int hard, int total) {

    public static final DifficultyStats EMPTY = new DifficultyStats(0, 0, 0, 0);
}
