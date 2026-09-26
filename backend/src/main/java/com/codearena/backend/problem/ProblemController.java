package com.codearena.backend.problem;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.backend.discussion.AddDiscussionRequest;
import com.codearena.backend.discussion.DiscussionResponse;
import com.codearena.backend.problem.dto.ProblemDetailResponse;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.security.AuthenticatedUser;
import com.codearena.backend.submission.SubmissionService;
import com.codearena.backend.submission.dto.SubmissionResultResponse;
import com.codearena.backend.submission.dto.SubmissionSummaryResponse;
import com.codearena.backend.submission.dto.SubmitRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;
    private final SubmissionService submissionService;

    public ProblemController(ProblemService problemService, SubmissionService submissionService) {
        this.problemService = problemService;
        this.submissionService = submissionService;
    }

    @GetMapping
    public ResponseEntity<List<ProblemSummaryResponse>> listAll() {
        return ResponseEntity.ok(problemService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemDetailResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(problemService.getById(id));
    }

    @GetMapping("/{id}/discussions")
    public ResponseEntity<List<DiscussionResponse>> getDiscussions(@PathVariable String id) {
        return ResponseEntity.ok(problemService.getDiscussions(id));
    }

    @PostMapping("/{id}/discussions")
    public ResponseEntity<DiscussionResponse> addDiscussion(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody AddDiscussionRequest request) {
        DiscussionResponse response = problemService.addDiscussion(id, principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<SubmissionSummaryResponse>> listMySubmissions(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(submissionService.listMineForProblem(principal.id(), id));
    }

    @PostMapping("/{id}/submissions")
    public ResponseEntity<SubmissionResultResponse> submit(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody SubmitRequest request) {
        SubmissionResultResponse response = submissionService.submit(id, principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
