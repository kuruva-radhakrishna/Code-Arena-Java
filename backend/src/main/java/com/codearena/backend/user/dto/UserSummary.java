package com.codearena.backend.user.dto;

import com.codearena.backend.user.User;

/**
 * Minimal, safe-to-expose view of a user, embedded wherever another resource
 * (a discussion, a leaderboard entry, ...) references a user by id.
 */
public record UserSummary(String id, String firstname, String lastname, String email) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail());
    }
}
