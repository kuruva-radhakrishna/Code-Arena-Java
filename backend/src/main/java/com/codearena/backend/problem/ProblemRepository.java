package com.codearena.backend.problem;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProblemRepository extends MongoRepository<Problem, String> {

    boolean existsByProblemName(String problemName);

    java.util.List<Problem> findByCreatedBy(String createdBy);
}
