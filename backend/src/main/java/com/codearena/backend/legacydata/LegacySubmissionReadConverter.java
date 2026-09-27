package com.codearena.backend.legacydata;

import static com.codearena.backend.legacydata.LegacyDocumentAccess.idString;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.instant;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.longValue;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.string;

import java.time.Instant;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.codearena.backend.submission.Language;
import com.codearena.backend.submission.Submission;
import com.codearena.backend.submission.Verdict;

/**
 * Reads a "submissions" document written by either app. The old app used
 * snake_case foreign keys (user_id/problem_id/contest_id) and stored the
 * verdict as a human-readable sentence ("Wrong Answer") rather than an enum
 * constant - both would otherwise throw on read (the id fields would just
 * come back null; the verdict/language enums would throw outright).
 */
@ReadingConverter
public class LegacySubmissionReadConverter implements Converter<Document, Submission> {

    @Override
    public Submission convert(Document doc) {
        return Submission.builder()
                .id(idString(doc, "_id"))
                .userId(idString(doc, "userId", "user_id"))
                .problemId(idString(doc, "problemId", "problem_id"))
                .contestId(idString(doc, "contestId", "contest_id"))
                .language(LegacyEnums.parseLenient(Language.class, string(doc, "language"), Language.PYTHON))
                .code(string(doc, "code"))
                .verdict(verdict(doc))
                .executionTimeMs(longValue(doc, "executionTimeMs", "executionTime"))
                .isInContest(Boolean.TRUE.equals(doc.get("isInContest")))
                .submittedAt(orNow(instant(doc, "submittedAt")))
                .build();
    }

    private static Verdict verdict(Document doc) {
        String raw = string(doc, "verdict");
        if (raw == null) {
            return Verdict.INTERNAL_ERROR;
        }
        // The old app's free-text verdicts that don't map onto an enum constant
        // even after the generic uppercase/underscore normalization.
        return switch (raw.trim()) {
            case "Syntax Error" -> Verdict.COMPILATION_ERROR;
            case "Unknown" -> Verdict.INTERNAL_ERROR;
            default -> LegacyEnums.parseLenient(Verdict.class, raw, Verdict.INTERNAL_ERROR);
        };
    }

    private static Instant orNow(Instant value) {
        return value != null ? value : Instant.now();
    }
}
