package com.codearena.backend.submission;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.backend.security.AuthenticatedUser;
import com.codearena.backend.submission.dto.SubmissionDetailResponse;
import com.codearena.backend.submission.dto.SubmissionSummaryResponse;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    public ResponseEntity<List<SubmissionSummaryResponse>> listMine(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(submissionService.listMine(principal.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDetailResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(submissionService.getById(id));
    }
}
