import React from 'react';
import './ProblemFilters.css';
import { FaListUl, FaCheckCircle, FaDatabase, FaCode, FaCogs, FaJs, FaHashtag, FaTable, FaLayerGroup } from 'react-icons/fa';

// Map some common topics to icons
const topicIcons = {
  array: <FaTable />, // Array
  string: <FaHashtag />, // String
  'hash table': <FaLayerGroup />,
  'dynamic programming': <FaCogs />,
  math: <FaDatabase />,
  sorting: <FaListUl />,
  greedy: <FaCode />,
  javascript: <FaJs />,
  // Add more as needed
};

const difficultyLabels = [
  { key: null, label: 'All' },
  { key: 'easy', label: 'Easy' },
  { key: 'medium', label: 'Medium' },
  { key: 'hard', label: 'Hard' },
  { key: 'solved', label: 'Solved' },
];

function ProblemFilters({
  problems = [],
  solvedSet = new Set(),
  selectedTopic,
  setSelectedTopic,
  selectedDifficulty,
  setSelectedDifficulty,
}) {
  // Build topic counts
  const topicCounts = {};
  problems.forEach(p => {
    if (Array.isArray(p.topics)) {
      p.topics.forEach(t => {
        const key = t.trim().toLowerCase();
        topicCounts[key] = (topicCounts[key] || 0) + 1;
      });
    } else if (p.topics) {
      const key = p.topics.trim().toLowerCase();
      topicCounts[key] = (topicCounts[key] || 0) + 1;
    }
  });
  // Sort topics by count desc
  const sortedTopics = Object.entries(topicCounts)
    .sort((a, b) => b[1] - a[1])
    .map(([topic, count]) => ({ topic, count }))
    .slice(0, 10); // Only top 10 topics

  return (
    <div className="problem-filters-container">
      {/* Topic filter bar */}
      <div className="problem-topic-bar">
        <button
          className={`topic-pill${!selectedTopic ? ' active' : ''}`}
          onClick={() => setSelectedTopic(null)}
        >
          <FaListUl style={{ marginRight: 6 }} /> All Topics
        </button>
        {sortedTopics.length === 0 && (
          <span style={{ color: 'red', marginLeft: 16, fontWeight: 500 }}>
            No topics found in problems data.
          </span>
        )}
        {sortedTopics.map(({ topic, count }) => (
          <button
            key={topic}
            className={`topic-pill${selectedTopic === topic ? ' active' : ''}`}
            onClick={() => setSelectedTopic(topic)}
          >
            <span style={{ marginRight: 6 }}>
              {topicIcons[topic] || <FaLayerGroup />}
            </span>
            {topic.charAt(0).toUpperCase() + topic.slice(1)}
            <span className="topic-badge">{count}</span>
          </button>
        ))}
      </div>
      {/* Difficulty/Solved filter bar */}
      <div className="problem-difficulty-bar">
        {difficultyLabels.map(({ key, label }) => (
          <button
            key={key || 'all'}
            className={`difficulty-pill${selectedDifficulty === key ? ' active' : ''}`}
            onClick={() => setSelectedDifficulty(key)}
          >
            {key === 'solved' ? <FaCheckCircle style={{ marginRight: 6, color: '#43a047' }} /> : null}
            {label}
          </button>
        ))}
      </div>
    </div>
  );
}

export default ProblemFilters; 