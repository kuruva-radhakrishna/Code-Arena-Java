package com.codearena.backend.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codearena.backend.compiler.CompilerClient;
import com.codearena.backend.compiler.CompilerClientException;
import com.codearena.backend.compiler.ExecuteRequest;
import com.codearena.backend.compiler.ExecuteResponse;
import com.codearena.backend.compiler.ExecutionStatus;
import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.contest.ContestService;
import com.codearena.backend.contest.LeaderboardEntry;
import com.codearena.backend.exception.ContestNotActiveException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.problem.ProblemService;
import com.codearena.backend.problem.TestCase;
import com.codearena.backend.submission.dto.ContestSubmissionResultResponse;
import com.codearena.backend.submission.dto.RunRequest;
import com.codearena.backend.submission.dto.SubmissionResultResponse;
import com.codearena.backend.submission.dto.SubmitRequest;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemService problemService;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ContestService contestService;

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private CompilerClient compilerClient;

    private SubmissionService submissionService;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionService(
                submissionRepository, problemService, problemRepository, contestService, contestRepository, compilerClient);
        when(submissionRepository.save(any())).thenAnswer(inv -> {
            Submission s = inv.getArgument(0);
            s.setId("submission-1");
            return s;
        });
    }

    private Problem problemWithTestCases(TestCase... testCases) {
        return Problem.builder().id("problem-1").problemName("Two Sum").testCases(List.of(testCases)).build();
    }

    @Test
    void submit_returnsAccepted_whenAllTestCasesPass() {
        Problem problem = problemWithTestCases(
                TestCase.builder().input("1 2").output("3").isPublic(true).build());
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problem);
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "3\n", null, 12));

        SubmissionResultResponse response = submissionService.submit("problem-1", "user-1", new SubmitRequest(Language.CPP, "code"));

        assertThat(response.verdict()).isEqualTo(Verdict.ACCEPTED);
        assertThat(response.failedCase()).isNull();
    }

    @Test
    void submit_returnsWrongAnswer_withFailedCaseDetails_forAPublicTestCase() {
        Problem problem = problemWithTestCases(
                TestCase.builder().input("1 2").output("3").isPublic(true).build());
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problem);
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "4\n", null, 12));

        SubmissionResultResponse response = submissionService.submit("problem-1", "user-1", new SubmitRequest(Language.CPP, "code"));

        assertThat(response.verdict()).isEqualTo(Verdict.WRONG_ANSWER);
        assertThat(response.failedCase()).isNotNull();
        assertThat(response.failedCase().expectedOutput()).isEqualTo("3");
        assertThat(response.failedCase().actualOutput()).isEqualTo("4\n");
    }

    @Test
    void submit_hidesFailedCaseDetails_whenTheFailingTestCaseIsHidden() {
        Problem problem = problemWithTestCases(
                TestCase.builder().input("secret-in").output("secret-out").isPublic(false).build());
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problem);
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "wrong", null, 12));

        SubmissionResultResponse response = submissionService.submit("problem-1", "user-1", new SubmitRequest(Language.CPP, "code"));

        assertThat(response.verdict()).isEqualTo(Verdict.WRONG_ANSWER);
        assertThat(response.failedCase()).isNull();
    }

    @Test
    void submit_stopsAtFirstFailure_andDoesNotRunRemainingTestCases() {
        Problem problem = problemWithTestCases(
                TestCase.builder().input("a").output("expected-a").isPublic(true).build(),
                TestCase.builder().input("b").output("expected-b").isPublic(true).build());
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problem);
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "wrong", null, 5));

        submissionService.submit("problem-1", "user-1", new SubmitRequest(Language.CPP, "code"));

        verify(compilerClient, times(1)).execute(any());
    }

    @Test
    void submit_recordsInternalError_whenCompilerServiceUnreachable() {
        Problem problem = problemWithTestCases(TestCase.builder().input("a").output("b").isPublic(true).build());
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problem);
        when(compilerClient.execute(any())).thenThrow(new CompilerClientException("down"));

        SubmissionResultResponse response = submissionService.submit("problem-1", "user-1", new SubmitRequest(Language.CPP, "code"));

        assertThat(response.verdict()).isEqualTo(Verdict.INTERNAL_ERROR);
    }

    private Contest liveContest(String... problemIds) {
        List<ContestProblem> problems = List.of(problemIds).stream()
                .map(id -> ContestProblem.builder().problemId(id).points(10).build())
                .toList();
        return Contest.builder()
                .id("contest-1")
                .contestTitle("Live Contest")
                .createdBy("admin-1")
                .problems(problems)
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
    }

    @Test
    void submitToContest_throws_whenContestNotCurrentlyActive() {
        Contest contest = Contest.builder()
                .id("contest-1").contestTitle("Future").createdBy("admin-1")
                .problems(List.of(ContestProblem.builder().problemId("problem-1").build()))
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();
        when(contestService.findContestOrThrow("contest-1")).thenReturn(contest);
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problemWithTestCases());

        assertThatThrownBy(() -> submissionService.submitToContest(
                "contest-1", "problem-1", "user-1", new SubmitRequest(Language.CPP, "code")))
                .isInstanceOf(ContestNotActiveException.class);
    }

    @Test
    void submitToContest_throws_whenProblemNotPartOfContest() {
        Contest contest = liveContest("other-problem");
        when(contestService.findContestOrThrow("contest-1")).thenReturn(contest);
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problemWithTestCases());

        assertThatThrownBy(() -> submissionService.submitToContest(
                "contest-1", "problem-1", "user-1", new SubmitRequest(Language.CPP, "code")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitToContest_awardsPointsOnFirstAccept_butNotOnResubmit() {
        Contest contest = liveContest("problem-1");
        Problem problem = problemWithTestCases(TestCase.builder().input("a").output("ok").isPublic(true).build());
        when(contestService.findContestOrThrow("contest-1")).thenReturn(contest);
        when(problemService.findProblemOrThrow("problem-1")).thenReturn(problem);
        when(compilerClient.execute(any())).thenReturn(new ExecuteResponse(ExecutionStatus.SUCCESS, "ok", null, 5));

        ContestSubmissionResultResponse first = submissionService.submitToContest(
                "contest-1", "problem-1", "user-1", new SubmitRequest(Language.CPP, "code"));
        assertThat(first.pointsAwarded()).isEqualTo(10);
        assertThat(first.totalPoints()).isEqualTo(10);

        ContestSubmissionResultResponse second = submissionService.submitToContest(
                "contest-1", "problem-1", "user-1", new SubmitRequest(Language.CPP, "code"));
        assertThat(second.pointsAwarded()).isEqualTo(0);
        assertThat(second.totalPoints()).isEqualTo(10);

        assertThat(contest.getLeaderBoard()).hasSize(1);
    }

    @Test
    void getById_returnsSubmission_whenNotHiddenByAnyLiveContest() {
        Submission submission = Submission.builder().id("sub-1").userId("user-1").problemId("problem-1")
                .language(Language.CPP).code("code").verdict(Verdict.ACCEPTED).build();
        when(submissionRepository.findById("sub-1")).thenReturn(Optional.of(submission));
        when(contestRepository.findLiveContests(any())).thenReturn(List.of());
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(Problem.builder().id("problem-1").problemName("Two Sum").build()));

        var response = submissionService.getById("sub-1");

        assertThat(response.problemName()).isEqualTo("Two Sum");
    }

    @Test
    void getById_hides_whenProblemLockedInADifferentLiveContest() {
        Submission submission = Submission.builder().id("sub-1").userId("user-1").problemId("problem-1")
                .language(Language.CPP).code("code").verdict(Verdict.ACCEPTED).build();
        when(submissionRepository.findById("sub-1")).thenReturn(Optional.of(submission));
        when(contestRepository.findLiveContests(any())).thenReturn(List.of(liveContest("problem-1")));

        assertThatThrownBy(() -> submissionService.getById("sub-1")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_shows_whenSubmissionBelongsToTheLiveContestItself() {
        Submission submission = Submission.builder().id("sub-1").userId("user-1").problemId("problem-1")
                .contestId("contest-1").language(Language.CPP).code("code").verdict(Verdict.ACCEPTED).isInContest(true).build();
        when(submissionRepository.findById("sub-1")).thenReturn(Optional.of(submission));
        when(contestRepository.findLiveContests(any())).thenReturn(List.of(liveContest("problem-1")));
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(Problem.builder().id("problem-1").problemName("Two Sum").build()));

        var response = submissionService.getById("sub-1");

        assertThat(response.contestId()).isEqualTo("contest-1");
    }

    @Test
    void run_delegatesDirectlyToCompilerClient_withoutPersisting() {
        ExecuteResponse expected = new ExecuteResponse(ExecutionStatus.SUCCESS, "output", null, 5);
        when(compilerClient.execute(new ExecuteRequest(Language.PYTHON, "print(1)", "in"))).thenReturn(expected);

        ExecuteResponse response = submissionService.run(new RunRequest(Language.PYTHON, "print(1)", "in"));

        assertThat(response).isEqualTo(expected);
        verify(submissionRepository, never()).save(any());
    }
}
