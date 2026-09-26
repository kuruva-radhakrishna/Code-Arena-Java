package com.codearena.compiler.execution;

final class ErrorMessages {

    private ErrorMessages() {
    }

    /** The last non-blank line of a compiler/runtime error stream - usually the most useful summary line. */
    static String lastNonBlankLine(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] lines = text.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].isBlank()) {
                return lines[i].strip();
            }
        }
        return "";
    }
}
