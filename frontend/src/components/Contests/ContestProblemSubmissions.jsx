import React from 'react';

function ContestProblemSubmissions({ submissions, refreshSubmissions }) {
    return (
        <div className="problem-submissions">
            <h3>Contest Problem Submissions</h3>
            <button onClick={refreshSubmissions} style={{ marginBottom: 12 }}>Refresh</button>
            <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 8 }}>
                <thead>
                    <tr>
                        <th>S.No</th>
                        <th>Language</th>
                        <th>Verdict</th>
                        <th>Submitted At</th>
                    </tr>
                </thead>
                <tbody>
                    {submissions && submissions.length > 0 ? (
                        submissions.map((sub, idx) => (
                            <tr key={sub.id}>
                                <td>{idx + 1}</td>
                                <td>{sub.language}</td>
                                <td>{sub.verdict}</td>
                                <td>{new Date(sub.submittedAt).toLocaleString()}</td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={4} style={{ textAlign: 'center' }}>No submissions found.</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
}

export default ContestProblemSubmissions; 