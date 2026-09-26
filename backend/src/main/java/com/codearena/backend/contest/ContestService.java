package com.codearena.backend.contest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.codearena.backend.contest.dto.ContestDetailResponse;
import com.codearena.backend.contest.dto.ContestProblemResponse;
import com.codearena.backend.contest.dto.ContestSummaryResponse;
import com.codearena.backend.contest.dto.LeaderboardEntryResponse;
import com.codearena.backend.contest.dto.ProblemStanding;
import com.codearena.backend.discussion.Discussion;
import com.codearena.backend.discussion.DiscussionResponse;
import com.codearena.backend.exception.ContestNotLiveException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.user.UserService;
import com.codearena.backend.user.dto.UserSummary;

@Service
public class ContestService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final UserService userService;

    public ContestService(ContestRepository contestRepository, ProblemRepository problemRepository,
            UserService userService) {
        this.contestRepository = contestRepository;
        this.problemRepository = problemRepository;
        this.userService = userService;
    }

    public List<ContestSummaryResponse> listAll() {
        return contestRepository.findAll().stream().map(ContestSummaryResponse::from).toList();
    }

    public ContestDetailResponse getById(String contestId, String requesterId) {
        Contest contest = findContestOrThrow(contestId);
        boolean isCreator = contest.getCreatedBy().equals(requesterId);

        if (!isCreator && !contest.hasStarted(Instant.now())) {
            throw new ContestNotLiveException();
        }

        Map<String, UserSummary> discussionAuthors = userService.summarize(
                contest.getDiscussions().stream().map(Discussion::getUserId).toList());
        List<DiscussionResponse> discussions = contest.getDiscussions().stream()
                .map(d -> DiscussionResponse.from(d, discussionAuthors.get(d.getUserId())))
                .toList();

        return new ContestDetailResponse(
                contest.getId(), contest.getContestTitle(), contest.getCreatedBy(), contest.getDescription(),
                contest.getStartTime(), contest.getEndTime(), isCreator,
                buildProblemResponses(contest), buildLeaderboard(contest), discussions);
    }

    public List<LeaderboardEntryResponse> getLeaderBoard(String contestId) {
        return buildLeaderboard(findContestOrThrow(contestId));
    }

    public Contest findContestOrThrow(String id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));
    }

    private List<ContestProblemResponse> buildProblemResponses(Contest contest) {
        Map<String, Problem> problemsById = problemRepository
                .findAllById(contest.getProblems().stream().map(ContestProblem::getProblemId).toList())
                .stream()
                .collect(Collectors.toMap(Problem::getId, p -> p));

        return contest.getProblems().stream()
                .sorted(Comparator.comparingInt(ContestProblem::getPoints))
                .map(cp -> new ContestProblemResponse(
                        cp.getProblemId(),
                        problemsById.containsKey(cp.getProblemId()) ? problemsById.get(cp.getProblemId()).getProblemName() : null,
                        cp.getPoints()))
                .toList();
    }

    private List<LeaderboardEntryResponse> buildLeaderboard(Contest contest) {
        List<String> problemIds = contest.getProblems().stream().map(ContestProblem::getProblemId).toList();
        Map<String, UserSummary> usersById = userService.summarize(
                contest.getLeaderBoard().stream().map(LeaderboardEntry::getUserId).toList());

        List<LeaderboardEntry> ranked = contest.getLeaderBoard().stream()
                .sorted(Comparator
                        .comparingInt(LeaderboardEntry::totalPoints).reversed()
                        .thenComparing(LeaderboardEntry::getLastSubmissionAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<LeaderboardEntryResponse> result = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            LeaderboardEntry entry = ranked.get(i);
            List<ProblemStanding> standings = new ArrayList<>();
            for (int p = 0; p < problemIds.size(); p++) {
                int points = p < entry.getPerProblemPoints().size() ? entry.getPerProblemPoints().get(p) : 0;
                Instant solvedAt = p < entry.getPerProblemSolvedAt().size() ? entry.getPerProblemSolvedAt().get(p) : null;
                standings.add(new ProblemStanding(problemIds.get(p), points, solvedAt));
            }
            result.add(new LeaderboardEntryResponse(
                    i + 1, usersById.get(entry.getUserId()), entry.totalPoints(),
                    entry.getLastSubmissionAt(), standings));
        }
        return result;
    }
}
