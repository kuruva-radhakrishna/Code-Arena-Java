package com.codearena.backend.problem;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.dto.AddDiscussionRequest;
import com.codearena.backend.problem.dto.DiscussionResponse;
import com.codearena.backend.problem.dto.ProblemDetailResponse;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;
import com.codearena.backend.user.dto.UserSummary;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public ProblemService(ProblemRepository problemRepository, UserRepository userRepository) {
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    public List<ProblemSummaryResponse> listAll() {
        return problemRepository.findAll().stream().map(ProblemSummaryResponse::from).toList();
    }

    public ProblemDetailResponse getById(String id) {
        return ProblemDetailResponse.from(findProblemOrThrow(id));
    }

    public List<DiscussionResponse> getDiscussions(String problemId) {
        Problem problem = findProblemOrThrow(problemId);
        Map<String, UserSummary> usersById = resolveUserSummaries(problem.getDiscussions());

        return problem.getDiscussions().stream()
                .map(discussion -> DiscussionResponse.from(discussion, usersById.get(discussion.getUserId())))
                .toList();
    }

    public DiscussionResponse addDiscussion(String problemId, String userId, AddDiscussionRequest request) {
        Problem problem = findProblemOrThrow(problemId);

        Discussion discussion = Discussion.builder()
                .id(new ObjectId().toHexString())
                .userId(userId)
                .comment(request.comment())
                .createdAt(Instant.now())
                .build();
        problem.getDiscussions().addFirst(discussion);
        problemRepository.save(problem);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return DiscussionResponse.from(discussion, UserSummary.from(user));
    }

    Problem findProblemOrThrow(String id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
    }

    private Map<String, UserSummary> resolveUserSummaries(List<Discussion> discussions) {
        List<String> userIds = discussions.stream().map(Discussion::getUserId).distinct().toList();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, UserSummary::from));
    }
}
