package com.codearena.backend.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.backend.admin.dto.AdminProblemResponse;
import com.codearena.backend.admin.dto.ContestRequest;
import com.codearena.backend.admin.dto.ProblemRequest;
import com.codearena.backend.contest.dto.ContestSummaryResponse;
import com.codearena.backend.problem.dto.ProblemSummaryResponse;
import com.codearena.backend.security.AuthenticatedUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/problems")
    public ResponseEntity<List<ProblemSummaryResponse>> listMyProblems(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(adminService.listMyProblems(principal.id()));
    }

    @PostMapping("/problems")
    public ResponseEntity<AdminProblemResponse> createProblem(
            @AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createProblem(principal.id(), request));
    }

    @PutMapping("/problems/{id}")
    public ResponseEntity<AdminProblemResponse> updateProblem(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(adminService.updateProblem(principal.id(), id, request));
    }

    @DeleteMapping("/problems/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser principal) {
        adminService.deleteProblem(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contests")
    public ResponseEntity<List<ContestSummaryResponse>> listMyContests(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(adminService.listMyContests(principal.id()));
    }

    @PostMapping("/contests")
    public ResponseEntity<ContestSummaryResponse> createContest(
            @AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody ContestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createContest(principal.id(), request));
    }

    @PutMapping("/contests/{id}")
    public ResponseEntity<ContestSummaryResponse> updateContest(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ContestRequest request) {
        return ResponseEntity.ok(adminService.updateContest(principal.id(), id, request));
    }

    @DeleteMapping("/contests/{id}")
    public ResponseEntity<Void> deleteContest(@PathVariable String id, @AuthenticationPrincipal AuthenticatedUser principal) {
        adminService.deleteContest(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}
