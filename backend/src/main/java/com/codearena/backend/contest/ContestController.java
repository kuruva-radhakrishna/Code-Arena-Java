package com.codearena.backend.contest;

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

import com.codearena.backend.contest.dto.ContestDetailResponse;
import com.codearena.backend.contest.dto.ContestSummaryResponse;
import com.codearena.backend.contest.dto.LeaderboardEntryResponse;
import com.codearena.backend.security.AuthenticatedUser;
import com.codearena.backend.submission.SubmissionService;
import com.codearena.backend.submission.dto.ContestSubmissionResultResponse;
import com.codearena.backend.submission.dto.SubmitRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;
    private final SubmissionService submissionService;

    public ContestController(ContestService contestService, SubmissionService submissionService) {
        this.contestService = contestService;
        this.submissionService = submissionService;
    }

    @GetMapping
    public ResponseEntity<List<ContestSummaryResponse>> listAll() {
        return ResponseEntity.ok(contestService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestDetailResponse> getById(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(contestService.getById(id, principal.id()));
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderBoard(@PathVariable String id) {
        return ResponseEntity.ok(contestService.getLeaderBoard(id));
    }

    @PostMapping("/{contestId}/problems/{problemId}/submissions")
    public ResponseEntity<ContestSubmissionResultResponse> submit(
            @PathVariable String contestId,
            @PathVariable String problemId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody SubmitRequest request) {
        ContestSubmissionResultResponse response =
                submissionService.submitToContest(contestId, problemId, principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
