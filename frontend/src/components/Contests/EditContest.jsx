import { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate, useParams } from 'react-router-dom';
import CircularProgress from '@mui/material/CircularProgress';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

function EditContest() {
  const [title, setTitle] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [allProblems, setAllProblems] = useState([]);
  const [selectedProblems, setSelectedProblems] = useState([]);
  const [description, setDescription] = useState('');
  const [aiLoading, setAiLoading] = useState(false);
  const [error, setError] = useState('');
  const [problemPoints, setProblemPoints] = useState({});
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { id } = useParams();

  useEffect(() => {
    const fetchContestAndProblems = async function () {
      try {
        console.log(id);
        const contestRes = await axios.get(`${BACKEND_URL}/contests/${id}`, { withCredentials: true });
        const problemsRes = await axios.get(`${BACKEND_URL}/problems/`, { withCredentials: true });
        console.log(contestRes);
        console.log(problemsRes);
        const contest = contestRes.data;
        setTitle(contest.contestTitle || '');
        setStartTime(contest.startTime ? contest.startTime.slice(0, 16) : '');
        setEndTime(contest.endTime ? contest.endTime.slice(0, 16) : '');
        setDescription(contest.description || '');
        setAllProblems(problemsRes.data || []);
        const selected = (contest.problems || []).map(p => p.problem_id._id || p.problem_id);
        setSelectedProblems(selected);
        const points = {};
        (contest.problems || []).forEach(p => {
          points[p.problem_id._id || p.problem_id] = p.points;
        });
        setProblemPoints(points);
      } catch (err) {
        setError('Failed to load contest or problems');
      }
      setLoading(false);
    }
    fetchContestAndProblems();
  }, [id]);

  const handleProblemSelect = (problemId) => {
    setSelectedProblems(prev =>
      prev.includes(problemId)
        ? prev.filter(id => id !== problemId)
        : [...prev, problemId]
    );
    setProblemPoints(prev =>
      prev[problemId] ? prev : { ...prev, [problemId]: 4 }
    );
  };

  const handlePointsChange = (problemId, value) => {
    setProblemPoints(prev => ({ ...prev, [problemId]: value }));
  };

  const validateContest = () => {
    if (!title || title.trim().length < 5) {
      return 'Contest title must be at least 5 characters.';
    }
    if (!startTime || !endTime) {
      return 'Start and end time are required.';
    }
    if (new Date(endTime) <= new Date(startTime)) {
      return 'End time must be after start time.';
    }
    if (!description || description.trim().length < 10) {
      return 'Description must be at least 10 characters.';
    }
    if (selectedProblems.length < 3) {
      return 'Select at least 3 problems for the contest.';
    }
    for (const pid of selectedProblems) {
      if (!problemPoints[pid] || isNaN(problemPoints[pid]) || problemPoints[pid] <= 0) {
        return 'Set valid points for all selected problems.';
      }
    }
    return '';
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const err = validateContest();
    if (err) {
      setError(err);
      return;
    }
    try {
      await axios.patch(`${BACKEND_URL}/admin/contest/${id}/update`, {
        contest: {
          contestTitle: title,
          startTime,
          endTime,
          problems: selectedProblems.map(pid => ({ problem_id: pid, points: Number(problemPoints[pid]) })),
          description,
        }
      }, { withCredentials: true });
      navigate('/contests');
    } catch (err) {
      console.log(err);
      setError(err.response?.data?.message || 'Failed to update contest');
    }
  };

  const handleAIComplete = async () => {
    setAiLoading(true);
    setError('');
    try {
      const res = await axios.post(`${BACKEND_URL}/ai/contest-description`, {
        contestTitle: title,
        problems: selectedProblems,
      }, { withCredentials: true });
      setDescription(res.data.description || '');
    } catch (err) {
      setError('AI completion failed');
    }
    setAiLoading(false);
  };

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}>
      <CircularProgress size={60} thickness={5} />
    </div>
  );

  return (
    <div className="new-contest-form-container" style={{ maxWidth: 600, margin: '40px auto', background: '#fff', borderRadius: 12, boxShadow: '0 2px 12px rgba(25,118,210,0.08)', padding: 32 }}>
      <h2 style={{ marginBottom: 24 }}>Edit Contest</h2>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 18 }}>
          <label>Contest Title</label><br />
          <input type="text" value={title} onChange={e => setTitle(e.target.value)} required style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} />
        </div>
        <div style={{ marginBottom: 18 }}>
          <label>Start Time</label><br />
          <input type="datetime-local" value={startTime} onChange={e => setStartTime(e.target.value)} required style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} />
        </div>
        <div style={{ marginBottom: 18 }}>
          <label>End Time</label><br />
          <input type="datetime-local" value={endTime} onChange={e => setEndTime(e.target.value)} required style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} />
        </div>
        <div style={{ marginBottom: 18 }}>
          <label>Select Problems (at least 3)</label><br />
          <div style={{ maxHeight: 260, overflowY: 'auto', border: '1px solid #eee', borderRadius: 8, padding: 8, background: '#fafbfc', display: 'flex', flexDirection: 'column', gap: 10 }}>
            {allProblems.length === 0 && <div>No problems found.</div>}
            {allProblems.map(problem => (
              <div key={problem._id} style={{
                display: 'flex',
                alignItems: 'center',
                gap: 16,
                background: selectedProblems.includes(problem._id) ? '#e3f2fd' : '#fff',
                border: selectedProblems.includes(problem._id) ? '2px solid #1976d2' : '1.5px solid #e0e0e0',
                borderRadius: 10,
                padding: '14px 18px',
                boxShadow: selectedProblems.includes(problem._id) ? '0 2px 8px rgba(25,118,210,0.08)' : 'none',
                transition: 'background 0.18s, border 0.18s, box-shadow 0.18s',
                cursor: 'pointer',
                minHeight: 72,
                position: 'relative',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
                  <input
                    type="checkbox"
                    checked={selectedProblems.includes(problem._id)}
                    onChange={() => handleProblemSelect(problem._id)}
                    style={{ marginRight: 16, accentColor: '#1976d2', width: 20, height: 20, alignSelf: 'flex-start' }}
                  />
                </div>
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                  <div style={{ fontWeight: 600, fontSize: '0.97rem', color: '#1976d2', marginBottom: 4 }}>{problem.problemName || problem._id}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginTop: 2 }}>
                    <span className={`difficulty-box ${problem.difficulty?.toLowerCase()}`} style={{ fontSize: '0.75rem', padding: '3px 10px', marginRight: 4 }}>{problem.difficulty}</span>
                    {problem.topics && problem.topics.length > 0 && (
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                        {problem.topics.map((topic, i) => (
                          <span key={i} className="topic-tag" style={{ fontSize: '0.75rem', padding: '2px 8px' }}>{topic}</span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
                {selectedProblems.includes(problem._id) && (
                  <input
                    type="number"
                    min={1}
                    value={problemPoints[problem._id] || 4}
                    onChange={e => handlePointsChange(problem._id, e.target.value)}
                    style={{ width: 70, padding: 6, borderRadius: 6, border: '1.5px solid #1976d2', fontWeight: 600, fontSize: '1rem', background: '#f5f7fa', color: '#1976d2', outline: 'none', marginLeft: 8, alignSelf: 'flex-start' }}
                    placeholder="Points"
                  />
                )}
              </div>
            ))}
          </div>
        </div>
        <div style={{ marginBottom: 18 }}>
          <label>Contest Description</label><br />
          <textarea value={description} onChange={e => setDescription(e.target.value)} rows={5} style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc', resize: 'vertical' }} />
          <button type="button" onClick={handleAIComplete} disabled={aiLoading} style={{ marginTop: 8, background: '#1976d2', color: '#fff', border: 'none', borderRadius: 6, padding: '7px 18px', fontWeight: 600, cursor: 'pointer' }}>{aiLoading ? 'Generating...' : 'AI Complete'}</button>
        </div>
        {error && <div style={{ color: 'red', marginBottom: 12 }}>{error}</div>}
        <button type="submit" style={{ background: '#1976d2', color: '#fff', border: 'none', borderRadius: 6, padding: '10px 22px', fontWeight: 600, fontSize: '1.08rem', cursor: 'pointer' }}>Update Contest</button>
      </form>
    </div>
  );
}

export default EditContest; 