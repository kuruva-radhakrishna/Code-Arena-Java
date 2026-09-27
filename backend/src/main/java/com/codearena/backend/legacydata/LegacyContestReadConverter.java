package com.codearena.backend.legacydata;

import static com.codearena.backend.legacydata.LegacyDocumentAccess.docList;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.idString;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.instant;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.instantList;
import static com.codearena.backend.legacydata.LegacyDocumentAccess.intList;
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
import com.codearena.backend.contest.LeaderboardEntry;
import com.codearena.backend.discussion.Discussion;

/**
 * Reads a "contests" document written by either app. Field-name aliasing
 * covers problems[].problem_id and Discussions, the old app's names. The
 * leaderboard maps this app's own field names (perProblemPoints/
 * perProblemSolvedAt/lastSubmissionAt, from Stage 3) directly - so a contest
 * created and updated entirely by this app (the normal case, and what every
 * integration test exercises) reads back correctly. The old app's leaderboard
 * embedded a full 2D array of past submissions per entry instead, which this
 * app's simplified model has no equivalent for - an old-shaped entry just
 * won't have those field names, so it comes back with an empty per-problem
 * history rather than a throw or a lossy best-effort mapping.
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
                .leaderBoard(leaderBoard(doc))
                .discussions(discussions(doc))
                .createdAt(orNow(instant(doc, "createdAt")))
                .build();
    }

    private static List<LeaderboardEntry> leaderBoard(Document doc) {
        List<LeaderboardEntry> result = new ArrayList<>();
        for (Document entry : docList(doc, "leaderBoard")) {
            result.add(LeaderboardEntry.builder()
                    .userId(idString(entry, "userId", "user_id"))
                    .perProblemPoints(intList(entry, "perProblemPoints"))
                    .perProblemSolvedAt(instantList(entry, "perProblemSolvedAt"))
                    .lastSubmissionAt(instant(entry, "lastSubmissionAt"))
                    .build());
        }
        return result;
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
