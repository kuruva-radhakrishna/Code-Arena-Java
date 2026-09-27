package com.codearena.backend.legacydata;

import static com.codearena.backend.legacydata.LegacyDocumentAccess.docList;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.idString;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.instant;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.intValue;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.string;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.codearena.backend.contest.Contest;
import com.codearena.backend.contest.ContestProblem;
import com.codearena.backend.discussion.Discussion;

/**
 * Reads a "contests" document written by either app. Only field-name aliasing
 * is needed here (no enum fields, so no crash risk) - problems[].problem_id
 * and Discussions are the old app's names. The old app's leaderboard embedded
 * a full 2D array of past submissions per entry; this app's simplified
 * {@code LeaderboardEntry} (Stage 3) has no equivalent, so old leaderboard
 * standings intentionally come back empty rather than attempting a lossy
 * best-effort mapping - contest metadata and its problem list still load
 * correctly.
 */
@ReadingConverter
public class LegacyContestReadConverter implements Converter<Document, Contest> {

    @Override
    public Contest convert(Document doc) {
        return Contest.builder()
                .id(idString(doc, "_id"))
                .contestTitle(string(doc, "contestTitle"))
                .createdBy(idString(doc, "createdBy"))
                .problems(problems(doc))
                .startTime(instant(doc, "startTime"))
                .endTime(instant(doc, "endTime"))
                .description(string(doc, "description"))
                .discussions(discussions(doc))
                .createdAt(orNow(instant(doc, "createdAt")))
                .build();
    }

    private static List<ContestProblem> problems(Document doc) {
        List<ContestProblem> result = new ArrayList<>();
        for (Document p : docList(doc, "problems")) {
            result.add(ContestProblem.builder()
                    .problemId(idString(p, "problemId", "problem_id"))
                    .points(intValue(p, 4, "points"))
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
