package com.codearena.backend.problem;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.discussion.AddDiscussionRequest;
import com.codearena.backend.discussion.Discussion;
import com.codearena.backend.discussion.DiscussionResponse;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.dto.ProblemDetailResponse;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.user.UserService;
import com.codearena.backend.user.dto.UserSummary;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserService userService;
    private final ContestRepository contestRepository;

    public ProblemService(ProblemRepository problemRepository, UserService userService,
            ContestRepository contestRepository) {
        this.problemRepository = problemRepository;
        this.userService = userService;
        this.contestRepository = contestRepository;
    }

    /**
     * Lists every problem except those currently locked inside a live contest
     * (so users can't practice a contest's problems while it's still running).
     */
    public List<ProblemSummaryResponse> listAll() {
        Set<String> lockedProblemIds = contestRepository.findLiveContests(Instant.now()).stream()
                .flatMap(contest -> contest.getProblems().stream())
                .map(ContestProblem::getProblemId)
                .collect(Collectors.toSet());

        return problemRepository.findAll().stream()
                .filter(problem -> !lockedProblemIds.contains(problem.getId()))
                .map(ProblemSummaryResponse::from)
                .toList();
    }

    public ProblemDetailResponse getById(String id) {
        return ProblemDetailResponse.from(findProblemOrThrow(id));
    }

    public List<DiscussionResponse> getDiscussions(String problemId) {
        Problem problem = findProblemOrThrow(problemId);
        Map<String, UserSummary> usersById = userService.summarize(
                problem.getDiscussions().stream().map(Discussion::getUserId).toList());

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

        UserSummary author = userService.summarize(List.of(userId)).get(userId);
        if (author == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return DiscussionResponse.from(discussion, author);
    }

    Problem findProblemOrThrow(String id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
    }
}
