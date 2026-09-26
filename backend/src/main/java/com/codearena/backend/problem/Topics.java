package com.codearena.backend.problem;

import java.util.Set;

/**
 * Canonical set of allowed topic tags for a problem, matching the original app's list.
 */
public final class Topics {

    public static final Set<String> ALLOWED = Set.of(
            "array", "string", "hash table", "dynamic programming", "math", "sorting", "greedy",
            "tree", "graph", "binary search", "recursion", "backtracking", "stack", "queue", "heap",
            "linked list", "sliding window", "two pointers", "bit manipulation", "number theory",
            "geometry", "database", "shell", "javascript", "concurrency", "depth-first search",
            "breadth-first search", "trie", "segment tree", "disjoint set", "topological sort",
            "shortest path", "minimum spanning tree", "game theory", "probability", "combinatorics",
            "implementation", "simulation", "other");

    private Topics() {
    }
}
