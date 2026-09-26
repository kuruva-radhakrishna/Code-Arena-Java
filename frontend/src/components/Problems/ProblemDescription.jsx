import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../../api/client";
import './ProblemDescription.css';
import ReactMarkdown from 'react-markdown';
import CircularProgress from '@mui/material/CircularProgress';

function ProblemDescription() {
    const { id } = useParams();
    const [problem, setProblem] = useState();
    const Navigate = useNavigate();
    const [showHints, setShowHints] = useState(false);
    const [hintsLoading, setHintsLoading] = useState(false);

    useEffect(() => {
        const fetchProblem = async () => {
            try {
                const result = await api.get(`/api/problems/${id}`);
                if (!result.data) {
                    alert("Selected problem has been deleted.");
                    Navigate("/problems");
                } else {
                    setProblem(result.data);
                }
            } catch {
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
                <span className={`difficulty-box ${problem.difficulty?.toLowerCase()}`} style={{ fontSize: '1.05rem', padding: '6px 20px' }}>{problem.difficulty?.toLowerCase()}</span>
            </div>
            {problem.topics && problem.topics.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '18px', marginTop: '12px' }}>
                    {problem.topics.map((topic, i) => (
                        <span key={i} className="topic-tag" style={{ fontSize: '0.98rem', padding: '5px 14px' }}>{topic}</span>
                    ))}
                </div>
            )}
            <ReactMarkdown>{problem.description}</ReactMarkdown>
            <h2>Constrains</h2>
            <ul>
                {problem.constraints.map((c)=>{
                    return <li>{c}</li>
                })}
            </ul>

            {problem.publicTestCases && problem.publicTestCases.length > 0 && (
                <>
                    <h2>Sample Input</h2>
                    <p>{problem.publicTestCases[0].input}</p>
                    <h2>Sample Output</h2>
                    <p>{problem.publicTestCases[0].output}</p>
                </>
            )}

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