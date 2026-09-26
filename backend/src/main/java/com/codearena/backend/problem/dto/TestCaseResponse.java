package com.codearena.backend.problem.dto;

import com.codearena.backend.problem.TestCase;

public record TestCaseResponse(String input, String output) {

    public static TestCaseResponse from(TestCase testCase) {
        return new TestCaseResponse(testCase.getInput(), testCase.getOutput());
    }
}
