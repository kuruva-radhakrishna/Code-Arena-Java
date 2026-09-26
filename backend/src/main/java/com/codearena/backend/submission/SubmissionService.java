package com.codearena.backend.submission;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.codearena.backend.compiler.CompilerClient;
import com.codearena.backend.compiler.CompilerClientException;
import com.codearena.backend.compiler.ExecuteRequest;
import com.codearena.backend.compiler.ExecuteResponse;
import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.contest.ContestService;
import com.codearena.backend.contest.LeaderboardEntry;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.exception.ContestNotActiveException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.problem.ProblemService;
import com.codearena.backend.problem.TestCase;
import com.codearena.backend.submission.dto.ContestSubmissionResultResponse;
import com.codearena.backend.submission.dto.FailedCaseResponse;
import com.codearena.backend.submission.dto.RunRequest;
import com.codearena.backend.submission.dto.SubmissionDetailResponse;
import com.codearena.backend.submission.dto.SubmissionResultResponse;
import com.codearena.backend.submission.dto.SubmissionSummaryResponse;
import com.codearena.backend.submission.dto.SubmitRequest;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemService problemService;
    private final ProblemRepository problemRepository;
    private final ContestService contestService;
    private final ContestRepository contestRepository;
    private final CompilerClient compilerClient;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemService problemService,
            ProblemRepository problemRepository,
            ContestService contestService,
            ContestRepository contestRepository,
            CompilerClient compilerClient) {
        this.submissionRepository = submissionRepository;
        this.problemService = problemService;
        this.problemRepository = problemRepository;
        this.contestService = contestService;
        this.contestRepository = contestRepository;
        this.compilerClient = compilerClient;
    }

    /** Ad hoc execution against a single input, not persisted and not graded. */
    public ExecuteResponse run(RunRequest request) {
        return compilerClient.execute(new ExecuteRequest(request.language(), request.code(), request.input()));
    }

    public SubmissionResultResponse submit(String problemId, String userId, SubmitRequest request) {
        Problem problem = problemService.findProblemOrThrow(problemId);
        GradingOutcome outcome = grade(request.language(), request.code(), problem.getTestCases());

        Submission submission = submissionRepository.save(Submission.builder()
                .userId(userId)
                .problemId(problemId)
                .language(request.language())
                .code(request.code())
                .verdict(outcome.verdict())
                .executionTimeMs(outcome.executionTimeMs())
                .isInContest(false)
                .build());

        return new SubmissionResultResponse(
                submission.getId(), outcome.verdict(), outcome.perCaseVerdicts(),
                toFailedCaseResponse(outcome), outcome.executionTimeMs());
    }

    public ContestSubmissionResultResponse submitToContest(
            String contestId, String problemId, String userId, SubmitRequest request) {
        Contest contest = contestService.findContestOrThrow(contestId);
        Problem problem = problemService.findProblemOrThrow(problemId);

        if (!contest.isLive(Instant.now())) {
            throw new ContestNotActiveException();
        }

        int problemIndex = indexOfProblem(contest, problemId);
        GradingOutcome outcome = grade(request.language(), request.code(), problem.getTestCases());

        Submission submission = submissionRepository.save(Submission.builder()
                .userId(userId)
                .problemId(problemId)
                .contestId(contestId)
                .language(request.language())
                .code(request.code())
                .verdict(outcome.verdict())
                .executionTimeMs(outcome.executionTimeMs())
                .isInContest(true)
                .build());

        LeaderboardUpdate update = updateLeaderboard(contest, userId, problemIndex, outcome.verdict());
        contestRepository.save(contest);

        SubmissionResultResponse result = new SubmissionResultResponse(
                submission.getId(), outcome.verdict(), outcome.perCaseVerdicts(),
                toFailedCaseResponse(outcome), outcome.executionTimeMs());
        return new ContestSubmissionResultResponse(result, update.pointsAwarded(), update.totalPoints());
    }

    public List<SubmissionSummaryResponse> listMine(String userId) {
        return toSummaries(submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId));
    }

    public List<SubmissionSummaryResponse> listMineForProblem(String userId, String problemId) {
        return toSummaries(submissionRepository.findByUserIdAndProblemIdOrderBySubmittedAtDesc(userId, problemId));
    }

    public SubmissionDetailResponse getById(String submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (isHiddenByAnotherLiveContest(submission)) {
            throw new ResourceNotFoundException("Submission not found");
        }

        String problemName = problemRepository.findById(submission.getProblemId())
                .map(Problem::getProblemName).orElse(null);

        return new SubmissionDetailResponse(
                submission.getId(), submission.getUserId(), submission.getProblemId(), problemName,
                submission.getContestId(), submission.getLanguage(), submission.getCode(), submission.getVerdict(),
                submission.getExecutionTimeMs(), submission.getSubmittedAt(), submission.isInContest());
    }

    /**
     * A submission for a problem that is currently locked inside a *different*
     * live contest is hidden — even from an authenticated viewer — so a
     * submission can't be used to read a contest's hidden test-case behavior
     * via another problem instance. Unlike the original app, this endpoint
     * always requires authentication; the original had no auth check at all.
     */
    private boolean isHiddenByAnotherLiveContest(Submission submission) {
        return contestRepository.findLiveContests(Instant.now()).stream()
                .filter(live -> live.getProblems().stream()
                        .anyMatch(p -> p.getProblemId().equals(submission.getProblemId())))
                .anyMatch(live -> !live.getId().equals(submission.getContestId()));
    }

    private int indexOfProblem(Contest contest, String problemId) {
        List<ContestProblem> problems = contest.getProblems();
        for (int i = 0; i < problems.size(); i++) {
            if (problems.get(i).getProblemId().equals(problemId)) {
                return i;
            }
        }
        throw new ResourceNotFoundException("This problem is not part of the contest");
    }

    private LeaderboardUpdate updateLeaderboard(Contest contest, String userId, int problemIndex, Verdict verdict) {
        LeaderboardEntry entry = contest.getLeaderBoard().stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst()
                .orElseGet(() -> {
                    int problemCount = contest.getProblems().size();
                    LeaderboardEntry created = LeaderboardEntry.builder()
                            .userId(userId)
                            .perProblemPoints(new ArrayList<>(Collections.nCopies(problemCount, 0)))
                            .perProblemSolvedAt(new ArrayList<>(Collections.nCopies(problemCount, null)))
                            .build();
                    contest.getLeaderBoard().add(created);
                    return created;
                });

        entry.setLastSubmissionAt(Instant.now());

        int pointsAwarded = 0;
        if (verdict == Verdict.ACCEPTED && entry.getPerProblemPoints().get(problemIndex) == 0) {
            pointsAwarded = contest.getProblems().get(problemIndex).getPoints();
            entry.getPerProblemPoints().set(problemIndex, pointsAwarded);
            entry.getPerProblemSolvedAt().set(problemIndex, entry.getLastSubmissionAt());
        }
        return new LeaderboardUpdate(pointsAwarded, entry.totalPoints());
    }

    private record LeaderboardUpdate(int pointsAwarded, int totalPoints) {
    }

    private List<SubmissionSummaryResponse> toSummaries(List<Submission> submissions) {
        Map<String, String> problemNamesById = problemRepository
                .findAllById(submissions.stream().map(Submission::getProblemId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Problem::getId, Problem::getProblemName));

        return submissions.stream()
                .map(s -> new SubmissionSummaryResponse(
                        s.getId(), s.getProblemId(), problemNamesById.get(s.getProblemId()),
                        s.getLanguage(), s.getVerdict(), s.getSubmittedAt(), s.isInContest(), s.getContestId()))
                .toList();
    }

    private GradingOutcome grade(Language language, String code, List<TestCase> testCases) {
        List<Verdict> perCaseVerdicts = new ArrayList<>();
        TestCase failedTestCase = null;
        String failedOutput = null;
        Long executionTimeMs = null;

        for (TestCase testCase : testCases) {
            ExecuteResponse response;
            try {
                response = compilerClient.execute(new ExecuteRequest(language, code, testCase.getInput()));
            } catch (CompilerClientException ex) {
                perCaseVerdicts.add(Verdict.INTERNAL_ERROR);
                failedTestCase = testCase;
                break;
            }

            executionTimeMs = response.executionTimeMs();
            Verdict verdict = toVerdict(response, testCase);
            perCaseVerdicts.add(verdict);
            if (verdict != Verdict.ACCEPTED) {
                failedTestCase = testCase;
                failedOutput = response.output();
                break;
            }
        }

        Verdict overall = perCaseVerdicts.isEmpty() ? Verdict.ACCEPTED : perCaseVerdicts.getLast();
        return new GradingOutcome(overall, perCaseVerdicts, failedTestCase, failedOutput, executionTimeMs);
    }

    private Verdict toVerdict(ExecuteResponse response, TestCase testCase) {
        return switch (response.status()) {
            case COMPILATION_ERROR -> Verdict.COMPILATION_ERROR;
            case RUNTIME_ERROR -> Verdict.RUNTIME_ERROR;
            case TIME_LIMIT_EXCEEDED -> Verdict.TIME_LIMIT_EXCEEDED;
            case SUCCESS -> normalize(response.output()).equals(normalize(testCase.getOutput()))
                    ? Verdict.ACCEPTED : Verdict.WRONG_ANSWER;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    private FailedCaseResponse toFailedCaseResponse(GradingOutcome outcome) {
        if (outcome.verdict() != Verdict.WRONG_ANSWER || outcome.failedTestCase() == null
                || !outcome.failedTestCase().isPublic()) {
            return null;
        }
        return new FailedCaseResponse(
                outcome.failedTestCase().getInput(), outcome.failedTestCase().getOutput(), outcome.failedOutput());
    }

    private record GradingOutcome(
            Verdict verdict, List<Verdict> perCaseVerdicts, TestCase failedTestCase, String failedOutput,
            Long executionTimeMs) {
    }
}
