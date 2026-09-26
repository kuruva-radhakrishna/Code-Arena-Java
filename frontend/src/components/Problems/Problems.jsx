import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import './Problems.css';
import api from "../../api/client";
import { useAuth } from "../../contexts/AuthContext";
import CircularProgress from '@mui/material/CircularProgress';
import ProblemFilters from './ProblemFilters';
import {
  CheckCircle as CheckCircleIcon,
  Code as CodeIcon,
  TrendingUp as TrendingUpIcon,
  Label as LabelIcon,
  Assignment as AssignmentIcon
} from '@mui/icons-material';

const DIFFICULTY_ORDER = { easy: 1, medium: 2, hard: 3 };

function Problems() {
    const [problems, setProblems] = useState([]);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(true);
    const [submissions, setSubmissions] = useState([]);
    const { user } = useAuth();
    const [solvedSet, setSolvedSet] = useState(new Set());
    const [selectedTopic, setSelectedTopic] = useState(null);
    const [selectedDifficulty, setSelectedDifficulty] = useState(null);

    // ✅ Use useEffect to call the data fetching function when the component mounts
    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError("");
            setSuccess("");
            try {
                const result = await api.get('/api/problems');
                if (!result || !result.data || result.data.length === 0) {
                    setError("Problems not found.");
                    setProblems([]);
                } else {
                    setProblems(result.data);
                }
            } catch {
                setError("Loading issues. Please try again later.");
                setProblems([]);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(""), 2000);
            return () => clearTimeout(timer);
        }
    }, [success]);

    useEffect(() => {
        const fetchSubmissions = async () => {
            if (!user) return;
            try {
                const res = await api.get('/api/submissions');
                setSubmissions(res.data || []);
            } catch {
                setSubmissions([]);
            }
        };
        fetchSubmissions();
    }, [user]);

    useEffect(() => {
        const set = new Set();
        (submissions || []).forEach(sub => {
            if (!sub || !sub.problemId) return; // null check
            if (sub.verdict === 'ACCEPTED') set.add(sub.problemId.toString());
        });
        setSolvedSet(set);
    }, [submissions]);

    // Filtering logic
    let filteredProblems = [...problems].filter(p => p && p.id); // filter out null/malformed
    if (selectedTopic) {
        filteredProblems = filteredProblems.filter(p => {
            if (Array.isArray(p.topics)) {
                return p.topics.map(t => t.trim().toLowerCase()).includes(selectedTopic);
            } else if (p.topics) {
                return p.topics.trim().toLowerCase() === selectedTopic;
            }
            return false;
        });
    }
    if (selectedDifficulty === 'solved') {
        filteredProblems = filteredProblems.filter(p => solvedSet.has(p.id?.toString()));
    } else if (selectedDifficulty) {
        filteredProblems = filteredProblems.filter(p => (p.difficulty || '').toLowerCase() === selectedDifficulty);
    }

    return (
        <div className="problems">
            {loading ? (
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}>
                    <CircularProgress size={60} thickness={5} />
                </div>
            ) : (
                <>
                    <ProblemFilters
                        problems={problems}
                        solvedSet={solvedSet}
                        selectedTopic={selectedTopic}
                        setSelectedTopic={setSelectedTopic}
                        selectedDifficulty={selectedDifficulty}
                        setSelectedDifficulty={setSelectedDifficulty}
                    />
                    {error && <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>}
                    {success && !error && <p style={{ color: 'green', textAlign: 'center' }}>{success}</p>}
                    {filteredProblems.length > 0 && (
                        <table>
                            <thead>
                                <tr>
                                    <th style={{ paddingRight: 32 }}>
                                        <span className="th-content">
                                            <AssignmentIcon />
                                            S.No
                                        </span>
                                    </th>
                                    <th style={{ paddingRight: 32 }}>
                                        <span className="th-content">
                                            <CodeIcon />
                                            Problem Name
                                        </span>
                                    </th>
                                    <th style={{ paddingRight: 32 }}>
                                        <span className="th-content">
                                            <TrendingUpIcon />
                                            Difficulty
                                        </span>
                                    </th>
                                    <th style={{ paddingRight: 32 }}>
                                        <span className="th-content">
                                            <LabelIcon />
                                            Topics
                                        </span>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredProblems.map((p, index) => {
                                    const isSolved = solvedSet.has(p.id?.toString());
                                    return (
                                        <tr key={index} className={isSolved ? 'solved-problem-row' : ''}>
                                            <td style={{ paddingRight: 32 }}>{index + 1}</td>
                                            <td style={{ paddingRight: 32 }}>
                                                <div className="problem-name-cell">
                                                    <Link to={`/problem/${p.id}/description`} className={isSolved ? 'solved-problem-link' : ''}>{p.problemName}</Link>
                                                    {isSolved && (
                                                        <span title="Solved" className="solved-tick" style={{ display: 'inline-flex', alignItems: 'center', marginLeft: 6 }}>
                                                            <CheckCircleIcon style={{ color: '#43a047', fontSize: '1.2rem' }} />
                                                        </span>
                                                    )}
                                                </div>
                                            </td>
                                            <td style={{ paddingRight: 32 }}>
                                                <span className={`difficulty-box ${p.difficulty?.toLowerCase()}`}>{p.difficulty?.toLowerCase()}</span>
                                            </td>
                                            <td style={{ paddingRight: 32 }}>
                                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                                                    {Array.isArray(p.topics)
                                                        ? p.topics.map((topic, i) => (
                                                            <span key={i} className="topic-tag">{topic}</span>
                                                        ))
                                                        : p.topics && <span className="topic-tag">{p.topics}</span>}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    )}
                </>
            )}
        </div>
    );
}

export default Problems;
