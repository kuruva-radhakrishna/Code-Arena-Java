package com.codearena.backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codearena.backend.admin.dto.AdminProblemResponse;
import com.codearena.backend.admin.dto.ContestProblemInput;
import com.codearena.backend.admin.dto.ContestRequest;
import com.codearena.backend.admin.dto.ProblemRequest;
import com.codearena.backend.admin.dto.TestCaseInput;
import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.contest.dto.ContestSummaryResponse;
import com.codearena.backend.exception.ForbiddenActionException;
import com.codearena.backend.exception.InvalidRequestException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.submission.Submission;
import com.codearena.backend.submission.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(problemRepository, contestRepository, submissionRepository);
    }

    private ProblemRequest validProblemRequest() {
        return new ProblemRequest(
                "Two Sum Problem",
                "Find two numbers in the array that add up to the target value",
                List.of("1 <= n <= 1000"),
                List.of(new TestCaseInput("1 2", "3", true)),
                Difficulty.EASY,
                List.of("array", "hash table"),
                List.of("Use a hash map"));
    }

    @Test
    void createProblem_savesAndReturnsFullDetail() {
        when(problemRepository.existsByProblemName(any())).thenReturn(false);
        when(problemRepository.save(any())).thenAnswer(inv -> {
            Problem p = inv.getArgument(0);
            p.setId("problem-1");
            return p;
        });

        AdminProblemResponse response = adminService.createProblem("admin-1", validProblemRequest());

        assertThat(response.id()).isEqualTo("problem-1");
        assertThat(response.testCases()).hasSize(1);
        assertThat(response.testCases().get(0).isPublic()).isTrue();
    }

    @Test
    void getMyProblem_returnsHiddenTestCasesToo_forTheOwningAdmin() {
        Problem problem = Problem.builder().id("problem-1").problemName("Two Sum").createdBy("admin-1")
                .testCases(List.of(
                        com.codearena.backend.problem.TestCase.builder().input("in").output("out").isPublic(false).build()))
                .build();
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(problem));

        AdminProblemResponse response = adminService.getMyProblem("admin-1", "problem-1");

        assertThat(response.testCases()).hasSize(1);
        assertThat(response.testCases().get(0).isPublic()).isFalse();
    }

    @Test
    void getMyProblem_throwsForbidden_whenNotOwner() {
        Problem problem = Problem.builder().id("problem-1").createdBy("someone-else").build();
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(problem));

        assertThatThrownBy(() -> adminService.getMyProblem("admin-1", "problem-1"))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void createProblem_rejectsDuplicateName() {
        when(problemRepository.existsByProblemName("Two Sum Problem")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createProblem("admin-1", validProblemRequest()))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createProblem_rejectsUnknownTopic() {
        ProblemRequest request = new ProblemRequest(
                "Two Sum Problem", "Find two numbers in the array that add up to the target value",
                List.of("constraint"), List.of(new TestCaseInput("in", "out", true)),
                Difficulty.EASY, List.of("not-a-real-topic"), List.of());

        assertThatThrownBy(() -> adminService.createProblem("admin-1", request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void updateProblem_throwsForbidden_whenNotOwner() {
        Problem existing = Problem.builder().id("problem-1").problemName("Old").createdBy("someone-else").build();
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> adminService.updateProblem("admin-1", "problem-1", validProblemRequest()))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void updateProblem_throws404_whenMissing() {
        when(problemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateProblem("admin-1", "missing", validProblemRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProblem_deletesProblemAndCascadesSubmissions() {
        Problem existing = Problem.builder().id("problem-1").problemName("Old").createdBy("admin-1").build();
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(existing));

        adminService.deleteProblem("admin-1", "problem-1");

        verify(problemRepository).delete(existing);
        verify(submissionRepository).deleteByProblemId("problem-1");
    }

    @Test
    void deleteProblem_throwsForbidden_whenNotOwner() {
        Problem existing = Problem.builder().id("problem-1").createdBy("someone-else").build();
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> adminService.deleteProblem("admin-1", "problem-1"))
                .isInstanceOf(ForbiddenActionException.class);
        verify(problemRepository, never()).delete(any());
    }

    private ContestRequest validContestRequest() {
        return new ContestRequest(
                "Weekly Contest One",
                "A fun weekly contest",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(2, ChronoUnit.DAYS),
                List.of(
                        new ContestProblemInput("p-1", null),
                        new ContestProblemInput("p-2", 10),
                        new ContestProblemInput("p-3", 20)));
    }

    @Test
    void createContest_defaultsMissingPoints_andValidatesProblemsExist() {
        when(contestRepository.existsByContestTitle(any())).thenReturn(false);
        when(problemRepository.findAllById(any())).thenReturn(List.of(
                Problem.builder().id("p-1").build(), Problem.builder().id("p-2").build(), Problem.builder().id("p-3").build()));
        when(contestRepository.save(any())).thenAnswer(inv -> {
            Contest c = inv.getArgument(0);
            c.setId("contest-1");
            return c;
        });

        ContestSummaryResponse response = adminService.createContest("admin-1", validContestRequest());

        assertThat(response.id()).isEqualTo("contest-1");
        assertThat(response.problemCount()).isEqualTo(3);

        ArgumentCaptor<Contest> captor = ArgumentCaptor.forClass(Contest.class);
        verify(contestRepository).save(captor.capture());
        assertThat(captor.getValue().getProblems())
                .extracting(ContestProblem::getPoints)
                .containsExactly(4, 10, 20);
    }

    @Test
    void createContest_rejects_whenAProblemIdDoesNotExist() {
        when(problemRepository.findAllById(any())).thenReturn(List.of(Problem.builder().id("p-1").build()));

        assertThatThrownBy(() -> adminService.createContest("admin-1", validContestRequest()))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createContest_rejects_whenEndTimeNotAfterStartTime() {
        ContestRequest request = new ContestRequest(
                "Weekly Contest One", "A fun weekly contest",
                Instant.now().plus(2, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS),
                List.of(new ContestProblemInput("p-1", 10), new ContestProblemInput("p-2", 10),
                        new ContestProblemInput("p-3", 10)));

        assertThatThrownBy(() -> adminService.createContest("admin-1", request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void updateContest_throwsForbidden_afterContestHasStarted() {
        Contest started = Contest.builder().id("contest-1").createdBy("admin-1")
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        when(contestRepository.findById("contest-1")).thenReturn(Optional.of(started));
        when(problemRepository.findAllById(any())).thenReturn(List.of(
                Problem.builder().id("p-1").build(), Problem.builder().id("p-2").build(), Problem.builder().id("p-3").build()));

        assertThatThrownBy(() -> adminService.updateContest("admin-1", "contest-1", validContestRequest()))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void deleteContest_deletesContest_andClearsContestIdFromItsSubmissions() {
        Contest existing = Contest.builder().id("contest-1").createdBy("admin-1").build();
        when(contestRepository.findById("contest-1")).thenReturn(Optional.of(existing));
        Submission submission = Submission.builder().id("sub-1").contestId("contest-1").build();
        when(submissionRepository.findByContestId("contest-1")).thenReturn(List.of(submission));

        adminService.deleteContest("admin-1", "contest-1");

        verify(contestRepository).delete(existing);
        assertThat(submission.getContestId()).isNull();
        verify(submissionRepository).saveAll(List.of(submission));
    }
}
