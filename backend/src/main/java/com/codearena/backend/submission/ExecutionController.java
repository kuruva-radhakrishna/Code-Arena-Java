package com.codearena.backend.submission;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.backend.compiler.ExecuteResponse;
import com.codearena.backend.submission.dto.RunRequest;

import jakarta.validation.Valid;

/**
 * Ad hoc code execution against a single input — not persisted, not graded
 * against a problem's test cases. Backs the editor's "Run" button.
 */
@RestController
public class ExecutionController {

    private final SubmissionService submissionService;

    public ExecutionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/api/execute")
    public ResponseEntity<ExecuteResponse> run(@Valid @RequestBody RunRequest request) {
        return ResponseEntity.ok(submissionService.run(request));
    }
}
