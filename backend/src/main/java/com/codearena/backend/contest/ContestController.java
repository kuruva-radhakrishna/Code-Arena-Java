package com.codearena.backend.contest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.backend.contest.dto.ContestDetailResponse;
import com.codearena.backend.contest.dto.ContestSummaryResponse;
import com.codearena.backend.contest.dto.LeaderboardEntryResponse;
import com.codearena.backend.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
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
}
