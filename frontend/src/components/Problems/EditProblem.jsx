import { useState, useEffect } from 'react';
import api from '../../api/client';
import ReactMarkdown from 'react-markdown';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './NewProblem.css';
import CircularProgress from '@mui/material/CircularProgress';

const ALLOWED_TOPICS = [
  "array", "string", "hash table", "dynamic programming", "math", "sorting", "greedy", "tree", "graph", "binary search", "recursion", "backtracking", "stack", "queue", "heap", "linked list", "sliding window", "two pointers", "bit manipulation", "number theory", "geometry", "database", "shell", "javascript", "concurrency", "depth-first search", "breadth-first search", "trie", "segment tree", "disjoint set", "topological sort", "shortest path", "minimum spanning tree", "game theory", "probability", "combinatorics", "implementation", "simulation", "other"
];

function EditProblem() {
  const { id } = useParams();
  const { user, loading } = useAuth();
  const navigate = useNavigate();
  const [problemName, setProblemName] = useState('');
  const [problemDescription, setProblemDescription] = useState('');
  const [constraints, setConstraints] = useState('');
  const [testCases, setTestCases] = useState([{ input: '', output: '', isPublic: true }]);
  const [difficulty, setDifficulty] = useState('medium');
  const [topics, setTopics] = useState([]);
  // Not editable in this form, but must be preserved on save - the backend
  // replaces the whole problem on update, unlike the original's partial $set.
  const [hints, setHints] = useState([]);
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [aiLoading, setAiLoading] = useState(false);

  useEffect(() => {
    const fetchProblem = async () => {
      try {
        // Full detail (including hidden test cases) for the owning admin -
        // fetching the public GET /api/problems/:id here would only return
        // public test cases and silently drop the hidden ones on save.
        const res = await api.get(`/api/admin/problems/${id}`);
        const p = res.data;
        setProblemName(p.problemName || '');
        setProblemDescription(p.description || '');
        setConstraints((p.constraints || []).join('\n'));
        setTestCases(p.testCases && p.testCases.length > 0 ? p.testCases : [{ input: '', output: '', isPublic: true }]);
        setDifficulty(p.difficulty ? p.difficulty.toLowerCase() : 'medium');
        setTopics(p.topics || []);
        setHints(p.hints || []);
      } catch {
        setMessage('Failed to fetch problem.');
      }
      setFetching(false);
    };
    fetchProblem();
  }, [id]);

  if (loading || fetching) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}>
      <CircularProgress size={60} thickness={5} />
    </div>
  );
  if (!user || user.role !== 'ADMIN') return <div>You are not authorized to edit problems.</div>;

  const handleTestCaseChange = (idx, field, value) => {
    setTestCases(tc => tc.map((t, i) => i === idx ? { ...t, [field]: value } : t));
  };
  const addTestCase = () => setTestCases(tc => [...tc, { input: '', output: '', isPublic: true }]);
  const removeTestCase = idx => setTestCases(tc => tc.filter((_, i) => i !== idx));

  const handleTopicChange = (e) => {
    const value = e.target.value;
    if (value && !topics.includes(value)) {
      setTopics([...topics, value]);
    }
  };
  const removeTopic = (topic) => {
    setTopics(topics.filter(t => t !== topic));
  };

  const handleAI = async () => {
    setMessage('');
    setAiLoading(true);
    try {
      const result = await api.post('/api/ai/problem-draft', {
        problemName,
        description: problemDescription || null,
        difficulty: difficulty ? difficulty.toUpperCase() : null,
        topics
      });

      const draft = result.data;
      if (draft) {
        setProblemName(draft.problemName || problemName);
        setProblemDescription(draft.description || problemDescription);
        setConstraints((draft.constraints || []).join('\n'));
        setTopics(draft.topics || []);
        setDifficulty(draft.difficulty ? draft.difficulty.toLowerCase() : difficulty);
        if (draft.testCases && Array.isArray(draft.testCases)) {
          setTestCases(draft.testCases);
        }
        if (draft.hints && Array.isArray(draft.hints)) {
          setHints(draft.hints);
        }
        setMessage('AI completed the problem!');
      } else {
        setMessage('AI did not return a valid problem.');
      }
    } catch (error) {
      console.log(error);
      setMessage(error.response?.data?.message || 'AI completion failed.');
    }
    setAiLoading(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setMessage('');
    try {
      await api.put(`/api/admin/problems/${id}`, {
        problemName,
        description: problemDescription,
        constraints: constraints.split('\n').map(s => s.trim()).filter(Boolean),
        testCases,
        difficulty: difficulty.toUpperCase(),
        topics,
        hints
      });
      setMessage('Problem updated successfully!');
      setTimeout(() => navigate('/profile'), 1200);
    } catch {
      setMessage('Error updating problem.');
    }
    setSubmitting(false);
  };

  return (
    <div className="new-problem-container">
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 8 }}>
        <h2 style={{ margin: 0 }}>Edit Problem</h2>
        <button
          onClick={handleAI}
          disabled={aiLoading}
          style={{
            background: aiLoading ? '#e0e0e0' : '#1976d2',
            color: aiLoading ? '#888' : '#fff',
            border: 'none',
            borderRadius: 7,
            padding: '8px 18px',
            fontWeight: 600,
            fontSize: '1rem',
            cursor: aiLoading ? 'not-allowed' : 'pointer',
            transition: 'background 0.18s, color 0.18s',
            minWidth: 120
          }}
        >
          {aiLoading ? 'Completing...' : 'AI completion'}
        </button>
      </div>
      {message && (
        <div style={{
          marginBottom: 16,
          color: message.includes('completed') ? 'green' : 'red',
          fontWeight: 600,
          fontSize: '1.1rem',
          minHeight: 24
        }}>{message}</div>
      )}
      <form onSubmit={handleSubmit} className="new-problem-form" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <label>Problem Name
          <input type="text" value={problemName} onChange={e => setProblemName(e.target.value)} required />
        </label>
        <label>Description (Markdown supported)
          <textarea value={problemDescription} onChange={e => setProblemDescription(e.target.value)} rows={6} required />
        </label>
        <div className="preview-box">
          <b>Preview:</b>
          <ReactMarkdown>{problemDescription}</ReactMarkdown>
        </div>
        <label>Constraints (one per line)
          <textarea value={constraints} onChange={e => setConstraints(e.target.value)} rows={2} required />
        </label>
        <label>Topics
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 8 }}>
            {topics.map(topic => (
              <span key={topic} className="topic-tag" style={{ background: '#1976d2', color: '#fff', borderRadius: 12, padding: '4px 12px', marginRight: 4, fontWeight: 500 }}>
                {topic}
                <button type="button" style={{ marginLeft: 6, background: 'none', border: 'none', color: '#fff', cursor: 'pointer', fontWeight: 700 }} onClick={() => removeTopic(topic)}>&times;</button>
              </span>
            ))}
            <select onChange={handleTopicChange} value="" style={{ minWidth: 120, padding: 6, borderRadius: 8 }}>
              <option value="">Add topic...</option>
              {ALLOWED_TOPICS.filter(t => !topics.includes(t)).map(t => (
                <option key={t} value={t}>{t.charAt(0).toUpperCase() + t.slice(1)}</option>
              ))}
            </select>
          </div>
        </label>
        <label>Difficulty
          <div className="difficulty-pill-group">
            {['easy', 'medium', 'hard'].map((level) => (
              <button
                type="button"
                key={level}
                className={`difficulty-pill${difficulty === level ? ' selected' : ''}${level !== 'easy' ? ' ' + level : ''}`}
                onClick={() => setDifficulty(level)}
                aria-pressed={difficulty === level}
              >
                {level.charAt(0).toUpperCase() + level.slice(1)}
              </button>
            ))}
          </div>
        </label>
        <div>
          <b>Test Cases</b>
          {testCases.map((tc, idx) => (
            <div key={idx} className="test-case-block">
              <label>Input
                <textarea value={tc.input} onChange={e => handleTestCaseChange(idx, 'input', e.target.value)} rows={2} required />
              </label>
              <label>Output
                <textarea value={tc.output} onChange={e => handleTestCaseChange(idx, 'output', e.target.value)} rows={2} required />
              </label>
              <label>Is Public?
                <input type="checkbox" checked={tc.isPublic} onChange={e => handleTestCaseChange(idx, 'isPublic', e.target.checked)} />
              </label>
              {testCases.length > 1 && <button type="button" onClick={() => removeTestCase(idx)}>Remove</button>}
            </div>
          ))}
          <button type="button" className="add-test-case-btn" onClick={addTestCase}>Add Test Case</button>
        </div>
        <button type="submit" disabled={submitting}>{submitting ? 'Saving...' : 'Save Changes'}</button>
      </form>
      {message && <div className="message" style={{ color: message.includes('success') ? 'green' : 'red' }}>{message}</div>}
    </div>
  );
}

export default EditProblem; 