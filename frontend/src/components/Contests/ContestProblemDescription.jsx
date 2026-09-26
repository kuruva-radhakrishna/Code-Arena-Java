import React from 'react';
import ReactMarkdown from 'react-markdown';

function ContestProblemDescription({ problem }) {
    if (!problem) return <div>Loading...</div>;
    return (
        <div className="problem-description" style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
            <h2 style={{ marginBottom: 12 }}>{problem.problemName}</h2>
            <div style={{ marginBottom: 18 }}>
                <ReactMarkdown>{problem.problemDescription}</ReactMarkdown>
            </div>
            <div style={{ marginBottom: 18 }}>
                <strong>Constraints:</strong>
                <ul>
                    {problem.Constraints && problem.Constraints.map((c, idx) => (
                        <li key={idx}>{c}</li>
                    ))}
                </ul>
            </div>
            <div style={{ marginBottom: 18 }}>
                <strong>Sample Input:</strong>
                <pre style={{ background: '#f6f8fa', padding: 10, borderRadius: 6 }}>{problem.TestCases && problem.TestCases[0]?.input}</pre>
            </div>
            <div style={{ marginBottom: 18 }}>
                <strong>Sample Output:</strong>
                <pre style={{ background: '#f6f8fa', padding: 10, borderRadius: 6 }}>{problem.TestCases && problem.TestCases[0]?.output}</pre>
            </div>
        </div>
    );
}

export default ContestProblemDescription; 