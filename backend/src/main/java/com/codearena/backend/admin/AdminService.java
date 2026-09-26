package com.codearena.backend.admin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.ProblemRepository;
import com.codearena.backend.problem.TestCase;
import com.codearena.backend.problem.Topics;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.submission.Submission;
import com.codearena.backend.submission.SubmissionRepository;

@Service
public class AdminService {

    private static final int DEFAULT_CONTEST_PROBLEM_POINTS = 4;

    private final ProblemRepository problemRepository;
    private final ContestRepository contestRepository;
    private final SubmissionRepository submissionRepository;

    public AdminService(
            ProblemRepository problemRepository,
            ContestRepository contestRepository,
            SubmissionRepository submissionRepository) {
        this.problemRepository = problemRepository;
        this.contestRepository = contestRepository;
        this.submissionRepository = submissionRepository;
    }

    // ---- Problems ----

    public List<ProblemSummaryResponse> listMyProblems(String userId) {
        return problemRepository.findByCreatedBy(userId).stream().map(ProblemSummaryResponse::from).toList();
    }

    public AdminProblemResponse createProblem(String userId, ProblemRequest request) {
        validateTopics(request.topics());
        if (problemRepository.existsByProblemName(request.problemName())) {
            throw new InvalidRequestException("A problem named '" + request.problemName() + "' already exists");
        }

        Problem problem = Problem.builder()
                .problemName(request.problemName())
                .description(request.description())
                .constraints(request.constraints())
                .testCases(toTestCases(request.testCases()))
                .difficulty(request.difficulty())
                .topics(request.topics())
                .hints(request.hints() == null ? List.of() : request.hints())
                .createdBy(userId)
                .build();

        return AdminProblemResponse.from(problemRepository.save(problem));
    }

    public AdminProblemResponse updateProblem(String userId, String problemId, ProblemRequest request) {
        validateTopics(request.topics());
        Problem problem = findProblemOwnedBy(problemId, userId);

        problem.setProblemName(request.problemName());
        problem.setDescription(request.description());
        problem.setConstraints(request.constraints());
        problem.setTestCases(toTestCases(request.testCases()));
        problem.setDifficulty(request.difficulty());
        problem.setTopics(request.topics());
        problem.setHints(request.hints() == null ? List.of() : request.hints());

        return AdminProblemResponse.from(problemRepository.save(problem));
    }

    public void deleteProblem(String userId, String problemId) {
        Problem problem = findProblemOwnedBy(problemId, userId);
        problemRepository.delete(problem);
        submissionRepository.deleteByProblemId(problemId);
    }

    private Problem findProblemOwnedBy(String problemId, String userId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        if (!problem.getCreatedBy().equals(userId)) {
            throw new ForbiddenActionException("You did not create this problem");
        }
        return problem;
    }

    private void validateTopics(List<String> topics) {
        for (String topic : topics) {
            if (!Topics.ALLOWED.contains(topic)) {
                throw new InvalidRequestException("Unknown topic: " + topic);
            }
        }
    }

    private List<TestCase> toTestCases(List<TestCaseInput> inputs) {
        return inputs.stream()
                .map(tc -> TestCase.builder().input(tc.input()).output(tc.output()).isPublic(tc.isPublic()).build())
                .toList();
    }

    // ---- Contests ----

    public List<ContestSummaryResponse> listMyContests(String userId) {
        return contestRepository.findByCreatedBy(userId).stream().map(ContestSummaryResponse::from).toList();
    }

    public ContestSummaryResponse createContest(String userId, ContestRequest request) {
        validateContestWindow(request);
        List<ContestProblem> problems = toContestProblems(request.problems());

        if (contestRepository.existsByContestTitle(request.contestTitle())) {
            throw new InvalidRequestException("A contest titled '" + request.contestTitle() + "' already exists");
        }

        Contest contest = Contest.builder()
                .contestTitle(request.contestTitle())
                .description(request.description())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .problems(problems)
                .createdBy(userId)
                .build();

        return ContestSummaryResponse.from(contestRepository.save(contest));
    }

    public ContestSummaryResponse updateContest(String userId, String contestId, ContestRequest request) {
        validateContestWindow(request);
        List<ContestProblem> problems = toContestProblems(request.problems());
        Contest contest = findContestOwnedBy(contestId, userId);

        if (contest.hasStarted(Instant.now())) {
            throw new ForbiddenActionException("A contest that has already started cannot be edited");
        }

        contest.setContestTitle(request.contestTitle());
        contest.setDescription(request.description());
        contest.setStartTime(request.startTime());
        contest.setEndTime(request.endTime());
        contest.setProblems(problems);

        return ContestSummaryResponse.from(contestRepository.save(contest));
    }

    public void deleteContest(String userId, String contestId) {
        Contest contest = findContestOwnedBy(contestId, userId);
        contestRepository.delete(contest);

        List<Submission> submissions = submissionRepository.findByContestId(contestId);
        submissions.forEach(s -> s.setContestId(null));
        submissionRepository.saveAll(submissions);
    }

    private Contest findContestOwnedBy(String contestId, String userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));
        if (!contest.getCreatedBy().equals(userId)) {
            throw new ForbiddenActionException("You did not create this contest");
        }
        return contest;
    }

    private void validateContestWindow(ContestRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidRequestException("endTime must be after startTime");
        }
    }

    private List<ContestProblem> toContestProblems(List<ContestProblemInput> inputs) {
        List<String> problemIds = inputs.stream().map(ContestProblemInput::problemId).toList();
        List<String> existingIds = problemRepository.findAllById(problemIds).stream().map(Problem::getId).toList();

        List<ContestProblem> result = new ArrayList<>();
        for (ContestProblemInput input : inputs) {
            if (!existingIds.contains(input.problemId())) {
                throw new InvalidRequestException("Problem not found: " + input.problemId());
            }
            int points = input.points() == null ? DEFAULT_CONTEST_PROBLEM_POINTS : input.points();
            if (points < 1) {
                throw new InvalidRequestException("points must be at least 1");
            }
            result.add(ContestProblem.builder().problemId(input.problemId()).points(points).build());
        }
        return result;
    }
}
