import { useParams, Link } from "react-router-dom";

function formatFormalDate(dateStr) {
  const date = new Date(dateStr);
  const day = date.getDate().toString().padStart(2, '0');
  const month = date.toLocaleString('en-GB', { month: 'long' });
  const year = date.getFullYear();
  let hour = date.getHours();
  const minute = date.getMinutes().toString().padStart(2, '0');
  const ampm = hour >= 12 ? 'pm' : 'am';
  hour = hour % 12;
  hour = hour ? hour : 12; // the hour '0' should be '12'
  return `${day} ${month} ${year} at ${hour}:${minute} ${ampm}`;
}

function ProblemSubmissions({ submissions, refreshSubmissions }) {
    return (
        <div className="problem-submissions">
            <table>
                <thead>
                    <tr>
                        <th>S.No</th>
                        <th>Language</th>
                        <th>Verdict</th>
                        <th>Submitted At</th>
                        <th>View</th>
                    </tr>
                </thead>
                <tbody>
                    {submissions && submissions.length > 0 ? (
                        submissions.map((submission, index) => (
                            <tr key={submission._id}>
                                <td>{index + 1}</td>
                                <td>{submission.language}</td>
                                <td>{submission.verdict}</td>
                                <td>{formatFormalDate(submission.submittedAt)}</td>
                                <td>
                                    <Link to={`/submission/${submission._id}`} className="view-code-link">Code</Link>
                                </td>
                                
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="5" style={{ textAlign: 'center' }}>No submissions found.</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
}

export default ProblemSubmissions;
