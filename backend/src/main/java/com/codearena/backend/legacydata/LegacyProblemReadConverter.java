package com.codearena.backend.legacydata;

import static com.codearena.backend.legacydata.LegacyDocumentAccess.docList;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.idString;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.instant;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.intValue;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.string;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.stringList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.codearena.backend.discussion.Discussion;
import com.codearena.backend.problem.Difficulty;
import com.codearena.backend.problem.Problem;
import com.codearena.backend.problem.TestCase;

/**
 * Reads a "problems" document written by either this app or the pre-rewrite
 * Node app (field names/casing: problemDescription/Constraints/TestCases/
 * CreatedBy/CreatedAt/Discussions, difficulty as lowercase text) - see
 * docs/STAGES.md for why the two apps share one database.
 */
@ReadingConverter
public class LegacyProblemReadConverter implements Converter<Document, Problem> {

    @Override
    public Problem convert(Document doc) {
        return Problem.builder()
                .id(idString(doc, "_id"))
                .problemName(string(doc, "problemName"))
                .description(string(doc, "description", "problemDescription"))
                .constraints(stringList(doc, "constraints", "Constraints"))
                .testCases(testCases(doc))
                .difficulty(LegacyEnums.parseLenient(Difficulty.class,
                        string(doc, "difficulty"), Difficulty.MEDIUM))
                .topics(stringList(doc, "topics"))
                .hints(stringList(doc, "hints"))
                .discussions(discussions(doc))
                .likes(intValue(doc, 0, "likes"))
                .dislikes(intValue(doc, 0, "dislikes"))
                .createdBy(idString(doc, "createdBy", "CreatedBy"))
                .createdAt(orNow(instant(doc, "createdAt", "CreatedAt")))
                .build();
    }

    private static List<TestCase> testCases(Document doc) {
        List<TestCase> result = new ArrayList<>();
        for (Document tc : docList(doc, "testCases", "TestCases")) {
            result.add(TestCase.builder()
                    .input(string(tc, "input"))
                    .output(string(tc, "output"))
                    .isPublic(!Boolean.FALSE.equals(tc.get("isPublic")))
                    .build());
        }
        return result;
    }

    private static List<Discussion> discussions(Document doc) {
        List<Discussion> result = new ArrayList<>();
        for (Document d : docList(doc, "discussions", "Discussions")) {
            result.add(Discussion.builder()
                    .userId(idString(d, "userId", "user"))
                    .comment(string(d, "comment"))
                    .likes(intValue(d, 0, "likes"))
                    .dislikes(intValue(d, 0, "dislikes"))
                    .createdAt(orNow(instant(d, "createdAt")))
                    .build());
        }
        return result;
    }

    private static Instant orNow(Instant value) {
        return value != null ? value : Instant.now();
    }
}
