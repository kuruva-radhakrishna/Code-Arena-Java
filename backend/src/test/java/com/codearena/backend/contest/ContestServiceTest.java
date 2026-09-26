package com.codearena.backend.contest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codearena.backend.contest.dto.ContestDetailResponse;
import com.codearena.backend.contest.dto.LeaderboardEntryResponse;
import com.codearena.backend.exception.ContestNotLiveException;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.user.UserService;
import com.codearena.backend.user.dto.UserSummary;

@ExtendWith(MockitoExtension.class)
class ContestServiceTest {

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserService userService;

    private ContestService contestService;

    @BeforeEach
    void setUp() {
        contestService = new ContestService(contestRepository, problemRepository, userService);
    }

    private Contest futureContest(String createdBy) {
        return Contest.builder()
                .id("contest-1")
                .contestTitle("Weekly Contest")
                .createdBy(createdBy)
                .problems(List.of(ContestProblem.builder().problemId("p-1").points(10).build()))
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void getById_throwsContestNotLive_whenNonCreatorRequestsBeforeStart() {
        when(contestRepository.findById("contest-1")).thenReturn(Optional.of(futureContest("admin-1")));

        assertThatThrownBy(() -> contestService.getById("contest-1", "someone-else"))
                .isInstanceOf(ContestNotLiveException.class);
    }

    @Test
    void getById_allowsCreator_evenBeforeStart() {
        when(contestRepository.findById("contest-1")).thenReturn(Optional.of(futureContest("admin-1")));
        when(problemRepository.findAllById(any())).thenReturn(List.of());
        when(userService.summarize(any())).thenReturn(Map.of());

        ContestDetailResponse response = contestService.getById("contest-1", "admin-1");

        assertThat(response.isCreator()).isTrue();
    }

    @Test
    void getById_throws_whenContestMissing() {
        when(contestRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contestService.getById("missing", "anyone"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_sortsProblemsByPointsAscending_andResolvesNames() {
        Contest contest = Contest.builder()
                .id("contest-2")
                .contestTitle("Sorted Contest")
                .createdBy("admin-1")
                .problems(List.of(
                        ContestProblem.builder().problemId("p-hard").points(20).build(),
                        ContestProblem.builder().problemId("p-easy").points(5).build()))
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        when(contestRepository.findById("contest-2")).thenReturn(Optional.of(contest));
        when(problemRepository.findAllById(any())).thenReturn(List.of(
                Problem.builder().id("p-hard").problemName("Hard One").build(),
                Problem.builder().id("p-easy").problemName("Easy One").build()));
        when(userService.summarize(any())).thenReturn(Map.of());

        ContestDetailResponse response = contestService.getById("contest-2", "some-viewer");

        assertThat(response.problems()).extracting("problemName").containsExactly("Easy One", "Hard One");
    }

    @Test
    void getLeaderBoard_ranksByPointsDesc_thenEarliestLastSubmission() {
        Contest contest = Contest.builder()
                .id("contest-3")
                .contestTitle("Ranked Contest")
                .createdBy("admin-1")
                .problems(List.of(ContestProblem.builder().problemId("p-1").points(10).build()))
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .leaderBoard(List.of(
                        LeaderboardEntry.builder().userId("low-scorer")
                                .perProblemPoints(List.of(5))
                                .perProblemSolvedAt(Collections.singletonList(null))
                                .lastSubmissionAt(Instant.now())
                                .build(),
                        LeaderboardEntry.builder().userId("late-finisher")
                                .perProblemPoints(List.of(10))
                                .perProblemSolvedAt(List.of(Instant.now()))
                                .lastSubmissionAt(Instant.now())
                                .build(),
                        LeaderboardEntry.builder().userId("early-finisher")
                                .perProblemPoints(List.of(10))
                                .perProblemSolvedAt(List.of(Instant.now().minus(1, ChronoUnit.HOURS)))
                                .lastSubmissionAt(Instant.now().minus(1, ChronoUnit.HOURS))
                                .build()))
                .build();
        when(contestRepository.findById("contest-3")).thenReturn(Optional.of(contest));
        when(userService.summarize(any())).thenReturn(Map.of(
                "low-scorer", new UserSummary("low-scorer", "Low", "Scorer", "low@example.com"),
                "late-finisher", new UserSummary("late-finisher", "Late", "Finisher", "late@example.com"),
                "early-finisher", new UserSummary("early-finisher", "Early", "Finisher", "early@example.com")));

        List<LeaderboardEntryResponse> leaderboard = contestService.getLeaderBoard("contest-3");

        assertThat(leaderboard).extracting(e -> e.user().id())
                .containsExactly("early-finisher", "late-finisher", "low-scorer");
        assertThat(leaderboard).extracting(LeaderboardEntryResponse::rank).containsExactly(1, 2, 3);
    }
}
