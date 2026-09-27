package com.codearena.backend.legacydata;

import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.codearena.backend.submission.Language;
import com.codearena.backend.submission.Submission;
import com.codearena.backend.submission.Verdict;

class LegacySubmissionReadConverterTest {

    private final LegacySubmissionReadConverter converter = new LegacySubmissionReadConverter();

    @Test
    void convert_readsLegacySnakeCaseForeignKeysAndFreeTextVerdict() {
        ObjectId userId = new ObjectId();
        ObjectId problemId = new ObjectId();
        Document legacy = new Document()
                .append("_id", new ObjectId())
                .append("user_id", userId)
                .append("problem_id", problemId)
                .append("language", "cpp")
                .append("code", "int main() {}")
                .append("verdict", "Wrong Answer")
                .append("executionTime", 123);

        Submission submission = converter.convert(legacy);

        assertThat(submission.getUserId()).isEqualTo(userId.toHexString());
        assertThat(submission.getProblemId()).isEqualTo(problemId.toHexString());
        assertThat(submission.getLanguage()).isEqualTo(Language.CPP);
        assertThat(submission.getVerdict()).isEqualTo(Verdict.WRONG_ANSWER);
        assertThat(submission.getExecutionTimeMs()).isEqualTo(123L);
    }

    @Test
    void convert_mapsLegacyFreeTextVerdictsWithoutADirectEnumMatch() {
        Document syntaxError = new Document().append("verdict", "Syntax Error");
        Document unknown = new Document().append("verdict", "Unknown");

        assertThat(converter.convert(syntaxError).getVerdict()).isEqualTo(Verdict.COMPILATION_ERROR);
        assertThat(converter.convert(unknown).getVerdict()).isEqualTo(Verdict.INTERNAL_ERROR);
    }

    @Test
    void convert_readsACurrentAppShapedDocumentToo() {
        ObjectId userId = new ObjectId();
        Document current = new Document()
                .append("_id", new ObjectId())
                .append("userId", userId)
                .append("language", "JAVA")
                .append("verdict", "ACCEPTED")
                .append("executionTimeMs", 45L);

        Submission submission = converter.convert(current);

        assertThat(submission.getUserId()).isEqualTo(userId.toHexString());
        assertThat(submission.getLanguage()).isEqualTo(Language.JAVA);
        assertThat(submission.getVerdict()).isEqualTo(Verdict.ACCEPTED);
        assertThat(submission.getExecutionTimeMs()).isEqualTo(45L);
    }
}
