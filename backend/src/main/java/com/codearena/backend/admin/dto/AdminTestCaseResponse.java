package com.codearena.backend.admin.dto;

import com.codearena.backend.problem.TestCase;

public record AdminTestCaseResponse(String input, String output, boolean isPublic) {

    public static AdminTestCaseResponse from(TestCase testCase) {
        return new AdminTestCaseResponse(testCase.getInput(), testCase.getOutput(), testCase.isPublic());
    }
}
