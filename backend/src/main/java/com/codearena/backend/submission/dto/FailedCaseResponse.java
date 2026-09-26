package com.codearena.backend.submission.dto;

/**
 * Details of the test case a submission failed on. Only ever populated when
 * that test case is public — unlike the original app, which returned the
 * input/expected/actual output of hidden test cases too, leaking them to
 * whoever submitted.
 */
public record FailedCaseResponse(String input, String expectedOutput, String actualOutput) {
}
