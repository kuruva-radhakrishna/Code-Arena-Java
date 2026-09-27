package com.codearena.backend.legacydata;

/**
 * The pre-rewrite Node app stored enum-like fields as lowercase/spaced strings
 * (e.g. "medium", "admin", "Wrong Answer") where this app uses upper-snake-case
 * constants (MEDIUM, ADMIN, WRONG_ANSWER). Spring Data's default enum binding
 * is exact-match only and throws on anything else, which would break every
 * read of a document written by the old app. This lenient parse - uppercase,
 * spaces to underscores, fall back rather than throw - covers that gap so the
 * shared database's existing documents remain readable.
 */
final class LegacyEnums {

    private LegacyEnums() {
    }

    static <E extends Enum<E>> E parseLenient(Class<E> type, String raw, E fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = raw.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        try {
            return Enum.valueOf(type, normalized);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
