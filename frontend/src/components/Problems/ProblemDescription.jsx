import { Routes, Route, Link, useParams, useNavigate } from "react-router-dom";
import ProblemSubmissions from "./ProblemSubmissions"; // create this component
import { useEffect, useState } from "react";
import axios from "axios";
import './ProblemDescription.css';
import ReactMarkdown from 'react-markdown';
import CircularProgress from '@mui/material/CircularProgress';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

function ProblemDescription() {
    const { id } = useParams();
    const [problem, setProblem] = useState();
    const Navigate = useNavigate();
    const [showHints, setShowHints] = useState(false);
    const [hintsLoading, setHintsLoading] = useState(false);

    useEffect(() => {
        const fetchProblem = async () => {
            try {
                const result = await axios.get(`${BACKEND_URL}/problems/${id}`, {
                    withCredentials: true,
                });
                if (!result.data) {
                    alert("Selected problem has been deleted.");
                    Navigate("/problems");
                } else {
                    setProblem(result.data);
                }
            } catch (err) {
                alert("Problem fetch error.");
                Navigate("/problems");
            }
        };

        fetchProblem();
    }, [id, Navigate]);
    
    if (!problem) {
        return <p>Loading problem...</p>;
    }

    const handleHintsClick = () => {
        setHintsLoading(true);
        setTimeout(() => {
            setShowHints(h => !h);
            setHintsLoading(false);
        }, 500); // Simulate loading
    };

    return ( 
        <div className="problem-description">
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '18px', marginBottom: '0px' }}>
                <h1 style={{ margin: 0, fontSize: '1.45rem', fontWeight: 700 }}>{problem.problemName}</h1>
                <span className={`difficulty-box ${problem.difficulty?.toLowerCase()}`} style={{ fontSize: '1.05rem', padding: '6px 20px' }}>{problem.difficulty}</span>
            </div>
            {problem.topics && problem.topics.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '18px', marginTop: '12px' }}>
                    {problem.topics.map((topic, i) => (
                        <span key={i} className="topic-tag" style={{ fontSize: '0.98rem', padding: '5px 14px' }}>{topic}</span>
                    ))}
                </div>
            )}
            <ReactMarkdown>{problem.problemDescription}</ReactMarkdown>
            <h2>Constrains</h2>
            <ul>
                {problem.Constraints.map((c)=>{
                    return <li>{c}</li>
                })}
            </ul>

            <h2>Sample Input</h2>
            <p>{problem.TestCases[0].input}</p>
            <h2>Sample Output</h2>
            <p>{problem.TestCases[0].output}</p>

            {problem.hints && problem.hints.length > 0 && (
                <div style={{ marginTop: 24 }}>
                    <button
                        onClick={handleHintsClick}
                        className="ai-review-btn"
                        disabled={hintsLoading}
                        style={{ marginBottom: 12 }}
                    >
                        {hintsLoading ? (
                            <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <CircularProgress size={18} style={{ color: '#fff' }} />
                                Loading...
                            </span>
                        ) : showHints ? 'Hide Hints' : 'View Hints'}
                    </button>
                    {showHints && (
                        <div>
                            <h2>Hints</h2>
                            <ul>
                                {problem.hints.map((h, i) => (
                                    <li key={i}>{h}</li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default ProblemDescription;