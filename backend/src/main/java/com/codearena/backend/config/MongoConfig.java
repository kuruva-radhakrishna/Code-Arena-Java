package com.codearena.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.codearena.backend.legacydata.LegacyContestReadConverter;
import com.codearena.backend.legacydata.LegacyProblemReadConverter;
import com.codearena.backend.legacydata.LegacySubmissionReadConverter;
import com.codearena.backend.legacydata.LegacyUserReadConverter;

/**
 * This app's database is shared with the pre-rewrite Node/Mongoose app (see
 * docs/STAGES.md) rather than a fresh one, so these documents' own read
 * converters have to tolerate that app's field names/casing alongside this
 * one's. See the com.codearena.backend.legacydata package for the converters
 * themselves.
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new LegacyProblemReadConverter(),
                new LegacyUserReadConverter(),
                new LegacySubmissionReadConverter(),
                new LegacyContestReadConverter()));
    }
}
