package com.codearena.backend.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codearena.backend.admin.AdminService;
import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.contest.ContestService;
import com.codearena.backend.contest.dto.LeaderboardEntryResponse;
import com.codearena.backend.contest.dto.ProblemStanding;
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.profile.dto.ProfileSummaryResponse;
import com.codearena.backend.submission.Submission;
import com.codearena.backend.submission.SubmissionRepository;
import com.codearena.backend.submission.SubmissionService;
import com.codearena.backend.submission.Verdict;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;
import com.codearena.backend.user.dto.UserSummary;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private ContestService contestService;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private AdminService adminService;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                userRepository, problemRepository, submissionRepository, contestRepository,
                contestService, submissionService, adminService);
    }

    @Test
    void getSummary_computesSolvedStatsFromDistinctAcceptedProblems() {
        User user = User.builder().id("user-1").firstname("Ada").lastname("Lovelace")
                .email("ada@example.com").role(Role.USER).build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(submissionService.listMine("user-1")).thenReturn(List.of());
        when(contestRepository.findByLeaderBoardUserId("user-1")).thenReturn(List.of());

        Submission accepted1 = Submission.builder().problemId("p-easy").verdict(Verdict.ACCEPTED).build();
        Submission acceptedDuplicate = Submission.builder().problemId("p-easy").verdict(Verdict.ACCEPTED).build();
        Submission accepted2 = Submission.builder().problemId("p-hard").verdict(Verdict.ACCEPTED).build();
        Submission wrong = Submission.builder().problemId("p-medium").verdict(Verdict.WRONG_ANSWER).build();
        when(submissionRepository.findByUserIdOrderBySubmittedAtDesc("user-1"))
                .thenReturn(List.of(accepted1, acceptedDuplicate, accepted2, wrong));
        when(problemRepository.findAllById(List.of("p-easy", "p-hard"))).thenReturn(List.of(
                Problem.builder().id("p-easy").difficulty(Difficulty.EASY).build(),
                Problem.builder().id("p-hard").difficulty(Difficulty.HARD).build()));
        when(problemRepository.findAll()).thenReturn(List.of(
                Problem.builder().id("p-easy").difficulty(Difficulty.EASY).build(),
                Problem.builder().id("p-medium").difficulty(Difficulty.MEDIUM).build(),
                Problem.builder().id("p-hard").difficulty(Difficulty.HARD).build()));

        ProfileSummaryResponse summary = profileService.getSummary("user-1");

        assertThat(summary.solvedStats().easy()).isEqualTo(1);
        assertThat(summary.solvedStats().hard()).isEqualTo(1);
        assertThat(summary.solvedStats().medium()).isEqualTo(0);
        assertThat(summary.solvedStats().total()).isEqualTo(2);
        assertThat(summary.problemTotals().total()).isEqualTo(3);
    }

    @Test
    void getSummary_omitsCreatedProblemsAndContests_forRegularUser() {
        User user = User.builder().id("user-1").role(Role.USER).build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(submissionService.listMine("user-1")).thenReturn(List.of());
        when(submissionRepository.findByUserIdOrderBySubmittedAtDesc("user-1")).thenReturn(List.of());
        when(problemRepository.findAllById(List.of())).thenReturn(List.of());
        when(problemRepository.findAll()).thenReturn(List.of());
        when(contestRepository.findByLeaderBoardUserId("user-1")).thenReturn(List.of());

        ProfileSummaryResponse summary = profileService.getSummary("user-1");

        assertThat(summary.problemsCreatedByMe()).isEmpty();
        assertThat(summary.contestsCreatedByMe()).isEmpty();
    }

    @Test
    void getSummary_includesAttendedContestWithRankAndPoints() {
        User user = User.builder().id("user-1").role(Role.USER).build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(submissionService.listMine("user-1")).thenReturn(List.of());
        when(submissionRepository.findByUserIdOrderBySubmittedAtDesc("user-1")).thenReturn(List.of());
        when(problemRepository.findAllById(List.of())).thenReturn(List.of());
        when(problemRepository.findAll()).thenReturn(List.of());

        Instant startTime = Instant.now().minus(Duration.ofDays(1));
        Contest contest = Contest.builder().id("contest-1").contestTitle("Weekly").startTime(startTime).build();
        when(contestRepository.findByLeaderBoardUserId("user-1")).thenReturn(List.of(contest));

        UserSummary userSummary = new UserSummary("user-1", "Ada", "Lovelace", "ada@example.com");
        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(
                2, userSummary, 15, Instant.now(), List.<ProblemStanding>of());
        when(contestService.getLeaderBoard("contest-1")).thenReturn(List.of(entry));

        ProfileSummaryResponse summary = profileService.getSummary("user-1");

        assertThat(summary.attendedContests()).hasSize(1);
        assertThat(summary.attendedContests().get(0).rank()).isEqualTo(2);
        assertThat(summary.attendedContests().get(0).totalPoints()).isEqualTo(15);
        assertThat(summary.attendedContests().get(0).contestTitle()).isEqualTo("Weekly");
    }
}
