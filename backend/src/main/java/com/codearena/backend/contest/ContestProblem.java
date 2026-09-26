package com.codearena.backend.contest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestProblem {

    private String problemId;

    @Builder.Default
    private int points = 4;
}
