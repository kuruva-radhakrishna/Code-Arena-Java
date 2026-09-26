package com.codearena.backend.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codearena.backend.exception.ResourceNotFoundException;
import com.codearena.backend.problem.dto.AddDiscussionRequest;
import com.codearena.backend.problem.dto.DiscussionResponse;
import com.codearena.backend.problem.dto.ProblemDetailResponse;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.user.Role;
import com.codearena.backend.user.User;
import com.codearena.backend.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    private ProblemService problemService;

    @BeforeEach
    void setUp() {
        problemService = new ProblemService(problemRepository, userRepository);
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
        when(userRepository.findById("user-2")).thenReturn(Optional.of(author));

        DiscussionResponse response = problemService.addDiscussion("problem-1", "user-2", new AddDiscussionRequest("new comment"));

        assertThat(response.comment()).isEqualTo("new comment");
        assertThat(response.user().email()).isEqualTo("grace@example.com");
        assertThat(problem.getDiscussions()).hasSize(2);
        assertThat(problem.getDiscussions().get(0).getComment()).isEqualTo("new comment");
        verify(problemRepository).save(problem);
    }

    @Test
    void getDiscussions_resolvesEachCommenter() {
        Problem problem = sampleProblem();
        problem.getDiscussions().add(Discussion.builder().id("d-1").userId("user-1").comment("hi").build());

        when(problemRepository.findById("problem-1")).thenReturn(Optional.of(problem));
        User commenter = User.builder().id("user-1").firstname("Ada").lastname("Lovelace").email("ada@example.com").role(Role.USER).build();
        when(userRepository.findAllById(any())).thenReturn(List.of(commenter));

        List<DiscussionResponse> responses = problemService.getDiscussions("problem-1");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).user().email()).isEqualTo("ada@example.com");
    }
}
