package com.codearena.backend.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.backend.ai.dto.ChatRequest;
import com.codearena.backend.ai.dto.ChatResponse;
import com.codearena.backend.ai.dto.ContestDescriptionRequest;
import com.codearena.backend.ai.dto.ContestDescriptionResponse;
import com.codearena.backend.ai.dto.DebugRequest;
import com.codearena.backend.ai.dto.DebugResponse;
import com.codearena.backend.ai.dto.ProblemDraftRequest;
import com.codearena.backend.ai.dto.ProblemDraftResponse;
import com.codearena.backend.ai.dto.ReviewRequest;
import com.codearena.backend.ai.dto.ReviewResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/review")
    public ResponseEntity<ReviewResponse> review(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(new ReviewResponse(aiService.reviewCode(request.code(), request.problemId())));
    }

    @PostMapping("/debug")
    public ResponseEntity<DebugResponse> debug(@Valid @RequestBody DebugRequest request) {
        return ResponseEntity.ok(new DebugResponse(aiService.debugCode(request.code(), request.problemDescription())));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(new ChatResponse(aiService.chat(request.message(), request.chatHistory())));
    }

    @PostMapping("/contest-description")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContestDescriptionResponse> contestDescription(
            @Valid @RequestBody ContestDescriptionRequest request) {
        String description = aiService.generateContestDescription(request.contestTitle(), request.problemNames());
        return ResponseEntity.ok(new ContestDescriptionResponse(description));
    }

    @PostMapping("/problem-draft")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemDraftResponse> problemDraft(@Valid @RequestBody ProblemDraftRequest request) {
        return ResponseEntity.ok(aiService.generateProblemDraft(request));
    }
}
