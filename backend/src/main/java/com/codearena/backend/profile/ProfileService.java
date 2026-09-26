package com.codearena.backend.profile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.codearena.backend.admin.AdminService;
import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.contest.ContestService;
import com.codearena.backend.contest.dto.ContestSummaryResponse;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.profile.dto.AttendedContestResponse;
import com.codearena.backend.profile.dto.DifficultyStats;
import com.codearena.backend.profile.dto.ProfileSummaryResponse;
import com.codearena.backend.submission.Submission;
import com.codearena.backend.submission.SubmissionRepository;
import com.codearena.backend.submission.SubmissionService;
import com.codearena.backend.submission.Verdict;
import com.codearena.backend.submission.dto.SubmissionSummaryResponse;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;
import com.codearena.backend.user.dto.UserResponse;

@Service
public class ProfileService {

    private static final int RECENT_SUBMISSIONS_LIMIT = 20;

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final ContestRepository contestRepository;
    private final ContestService contestService;
    private final SubmissionService submissionService;
    private final AdminService adminService;

    public ProfileService(
            UserRepository userRepository,
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository,
            ContestRepository contestRepository,
            ContestService contestService,
            SubmissionService submissionService,
            AdminService adminService) {
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.contestRepository = contestRepository;
        this.contestService = contestService;
        this.submissionService = submissionService;
        this.adminService = adminService;
    }

    public ProfileSummaryResponse getSummary(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isAdmin = user.getRole() == Role.ADMIN;

        List<SubmissionSummaryResponse> recentSubmissions = submissionService.listMine(userId).stream()
                .limit(RECENT_SUBMISSIONS_LIMIT)
                .toList();

        List<ProblemSummaryResponse> problemsCreatedByMe = isAdmin ? adminService.listMyProblems(userId) : List.of();
        List<ContestSummaryResponse> contestsCreatedByMe = isAdmin ? adminService.listMyContests(userId) : List.of();

        return new ProfileSummaryResponse(
                UserResponse.from(user),
                computeSolvedStats(userId),
                computeProblemTotals(),
                problemsCreatedByMe,
                recentSubmissions,
                contestsCreatedByMe,
                computeAttendedContests(userId));
    }

    private DifficultyStats computeSolvedStats(String userId) {
        List<String> solvedProblemIds = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .filter(s -> s.getVerdict() == Verdict.ACCEPTED)
                .map(Submission::getProblemId)
                .distinct()
                .toList();
        return tallyByDifficulty(problemRepository.findAllById(solvedProblemIds));
    }

    private DifficultyStats computeProblemTotals() {
        return tallyByDifficulty(problemRepository.findAll());
    }

    private DifficultyStats tallyByDifficulty(List<Problem> problems) {
        Map<Difficulty, Long> counts = problems.stream()
                .collect(Collectors.groupingBy(Problem::getDifficulty, Collectors.counting()));
        int easy = counts.getOrDefault(Difficulty.EASY, 0L).intValue();
        int medium = counts.getOrDefault(Difficulty.MEDIUM, 0L).intValue();
        int hard = counts.getOrDefault(Difficulty.HARD, 0L).intValue();
        return new DifficultyStats(easy, medium, hard, easy + medium + hard);
    }

    private List<AttendedContestResponse> computeAttendedContests(String userId) {
        return contestRepository.findByLeaderBoardUserId(userId).stream()
                .map(contest -> toAttendedContest(contest, userId))
                .filter(Objects::nonNull)
                .toList();
    }

    private AttendedContestResponse toAttendedContest(Contest contest, String userId) {
        return contestService.getLeaderBoard(contest.getId()).stream()
                .filter(entry -> entry.user() != null && entry.user().id().equals(userId))
                .findFirst()
                .map(entry -> new AttendedContestResponse(
                        contest.getId(), contest.getContestTitle(), contest.getStartTime(),
                        entry.rank(), entry.totalPoints(), entry.lastSubmissionAt()))
                .orElse(null);
    }
}
