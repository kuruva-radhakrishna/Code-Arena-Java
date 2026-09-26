package com.codearena.backend.submission;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubmissionRepository extends MongoRepository<Submission, String> {

    List<Submission> findByUserIdOrderBySubmittedAtDesc(String userId);

    List<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(String userId, String problemId);
}
