package com.codearena.backend.contest;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ContestRepository extends MongoRepository<Contest, String> {

    boolean existsByContestTitle(String contestTitle);

    List<Contest> findByCreatedBy(String createdBy);

    @Query("{ 'startTime': { '$lte': ?0 }, 'endTime': { '$gte': ?0 } }")
    List<Contest> findLiveContests(Instant now);
}
