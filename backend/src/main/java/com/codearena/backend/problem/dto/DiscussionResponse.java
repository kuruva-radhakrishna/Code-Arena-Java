package com.codearena.backend.problem.dto;

import java.time.Instant;

import com.codearena.backend.problem.Discussion;
import com.codearena.backend.user.dto.UserSummary;

public record DiscussionResponse(
        String id,
        UserSummary user,
        String comment,
        int likes,
        int dislikes,
        Instant createdAt) {

    public static DiscussionResponse from(Discussion discussion, UserSummary user) {
        return new DiscussionResponse(
                discussion.getId(), user, discussion.getComment(), discussion.getLikes(),
                discussion.getDislikes(), discussion.getCreatedAt());
    }
}
