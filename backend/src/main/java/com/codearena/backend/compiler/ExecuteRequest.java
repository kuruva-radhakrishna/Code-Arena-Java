package com.codearena.backend.compiler;

import com.codearena.backend.submission.Language;

public record ExecuteRequest(Language language, String code, String input) {
}
