package com.codearena.backend.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.contest.ContestRepository;
import com.codearena.backend.discussion.AddDiscussionRequest;
import com.codearena.backend.discussion.Discussion;
import com.codearena.backend.discussion.DiscussionResponse;
import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.dto.ProblemDetailResponse;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserService;
import com.codearena.backend.user.dto.UserSummary;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserService userService;

    @Mock
    private ContestRepository contestRepository;

    private ProblemService problemService;

    @BeforeEach
    void setUp() {
        problemService = new ProblemService(problemRepository, userService, contestRepository);
    }

    private Problem sampleProblem() {
        return Problem.builder()
                .id("problem-1")
                .problemName("Two Sum")
                .description("Find two numbers that add up to target")
                .difficulty(Difficulty.EASY)
                .topics(List.of("array", "hash table"))
                .testCases(List.of(
                        TestCase.builder().input("in-public").output("out-public").isPublic(true).build(),
                        TestCase.builder().input("in-hidden").output("out-hidden").isPublic(false).build()))
                .createdBy("admin-1")
                .build();
    }

    @Test
    void listAll_mapsEachProblemToASummary() {
        when(problemRepository.findAll()).thenReturn(List.of(sampleProblem()));

        List<ProblemSummaryResponse> result = problemService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).problemName()).isEqualTo("Two Sum");
    }

    @Test
    void listAll_excludesProblemsCurrentlyLockedInALiveContest() {
        Problem lockedProblem = sampleProblem();
        Problem freeProblem = Problem.builder().id("problem-2").problemName("Reverse String").build();
        when(problemRepository.findAll()).thenReturn(List.of(lockedProblem, freeProblem));

        Contest liveContest = Contest.builder()
                .id("contest-1")
                .problems(List.of(ContestProblem.builder().problemId("problem-1").build()))
                .build();
        when(contestRepository.findLiveContests(any())).thenReturn(List.of(liveContest));

        List<ProblemSummaryResponse> result = problemService.listAll();

        assertThat(result).extracting(ProblemSummaryResponse::id).containsExactly("problem-2");
    }

    @Test
    void getById_hidesNonPublicTestCases() {
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(sampleProblem()));

        ProblemDetailResponse response = problemService.getById("problem-1");

        assertThat(response.publicTestCases()).hasSize(1);
        assertThat(response.publicTestCases().get(0).input()).isEqualTo("in-public");
    }

    @Test
    void getById_throws_whenProblemMissing() {
        when(problemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.getById("missing")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addDiscussion_insertsAtFront_andReturnsResolvedUser() {
        Problem problem = sampleProblem();
        Discussion existing = Discussion.builder().id("d-1").userId("user-1").comment("first comment").build();
        problem.getDiscussions().add(existing);

        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(problem));
        User author = User.builder().id("user-2").firstname("Grace").lastname("Hopper").email("grace@example.com").role(Role.USER).build();
        when(userService.summarize(List.of("user-2"))).thenReturn(Map.of("user-2", UserSummary.from(author)));

        DiscussionResponse response = problemService.addDiscussion("problem-1", "user-2", new AddDiscussionRequest("new comment"));

        assertThat(response.comment()).isEqualTo("new comment");
        assertThat(response.user().email()).isEqualTo("grace@example.com");
        assertThat(problem.getDiscussions()).hasSize(2);
        assertThat(problem.getDiscussions().get(0).getComment()).isEqualTo("new comment");
        verify(problemRepository).save(problem);
    }

    @Test
    void addDiscussion_throws_whenAuthorNoLongerExists() {
        Problem problem = sampleProblem();
        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(problem));
        when(userService.summarize(List.of("ghost-user"))).thenReturn(Map.of());

        assertThatThrownBy(() -> problemService.addDiscussion("problem-1", "ghost-user", new AddDiscussionRequest("hi")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDiscussions_resolvesEachCommenter() {
        Problem problem = sampleProblem();
        problem.getDiscussions().add(Discussion.builder().id("d-1").userId("user-1").comment("hi").build());

        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(problem));
        User commenter = User.builder().id("user-1").firstname("Ada").lastname("Lovelace").email("ada@example.com").role(Role.USER).build();
        when(userService.summarize(any())).thenReturn(Map.of("user-1", UserSummary.from(commenter)));

        List<DiscussionResponse> responses = problemService.getDiscussions("problem-1");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).user().email()).isEqualTo("ada@example.com");
    }
}
