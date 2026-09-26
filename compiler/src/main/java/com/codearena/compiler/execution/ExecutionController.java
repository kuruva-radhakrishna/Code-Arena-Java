package com.codearena.compiler.execution;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.compiler.execution.dto.ExecuteRequest;
import com.codearena.compiler.execution.dto.ExecuteResponse;

import jakarta.validation.Valid;

@RestController
public class ExecutionController {

    private final CodeExecutionService codeExecutionService;

    public ExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/api/execute")
    public ResponseEntity<ExecuteResponse> execute(@Valid @RequestBody ExecuteRequest request) {
        return ResponseEntity.ok(codeExecutionService.execute(request));
    }
}
