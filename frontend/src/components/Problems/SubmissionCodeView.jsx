import { useParams, Link, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import axios from 'axios';
import './ProblemSubmissions.css';
import CircularProgress from '@mui/material/CircularProgress';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

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

function SubmissionCodeView() {
  const { id, contestId } = useParams();
  const location = useLocation();
  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // Try to get contestId from params or location.state
  const contestIdParam = contestId || (location.state && location.state.contestId);

  useEffect(() => {
    const fetchSubmission = async () => {
      try {
        setLoading(true);
        const res = await axios.get(`${BACKEND_URL}/submissions/single/${id}`, { withCredentials: true });
        setSubmission(res.data);
        setError(null);
      } catch (err) {
        let msg = 'Could not fetch submission.';
        if (err.response && err.response.data && err.response.data.message) {
          msg = err.response.data.message;
        }
        setError(msg);
        setSubmission(null);
      } finally {
        setLoading(false);
      }
    };
    fetchSubmission();
  }, [id]);

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}><CircularProgress size={60} thickness={5} /></div>;
  if (error) {
    let displayMsg = error;
    if (error === 'Submission not found') {
      displayMsg = 'Submission not found.';
    } else if (error === 'You are not allowed to view this submission during the contest.') {
      displayMsg = 'This problem is part of an ongoing contest. You are not allowed to view this submission now.';
    }
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '30vh' }}>
        <div style={{
          background: '#fff',
          color: '#e53935',
          padding: '32px 24px',
          borderRadius: '16px',
          boxShadow: '0 2px 16px rgba(25,118,210,0.08)',
          fontSize: '1.25rem',
          fontWeight: 500,
          textAlign: 'center',
          maxWidth: 700,
          width: '100%'
        }}>
          {displayMsg}
        </div>
      </div>
    );
  }
  if (!submission) return <div className="problem-submissions"><p>No submission found.</p></div>;

  // Contest constraint logic
  

  return (
    <div className="problem-submissions" style={{maxWidth: 700, margin: '32px auto'}}>
      <h2 style={{marginBottom: 12}}>Submission Details</h2>
      <div style={{marginBottom: 16}}>
        <b>Language:</b> {submission.language} <br/>
        <b>Verdict:</b> {submission.verdict} <br/>
        <b>Submitted At:</b> {formatFormalDate(submission.submittedAt)} <br/>
      </div>
      <div>
        <b>Code:</b>
        <pre style={{
          background: '#f5f7fa',
          borderRadius: 8,
          padding: 16,
          fontFamily: 'Fira Mono, Consolas, monospace',
          fontSize: '1rem',
          overflowX: 'auto',
          marginTop: 8,
          marginBottom: 16
        }}>{submission.code}</pre>
      </div>
      <Link to={-1} className="view-code-link">Back</Link>
    </div>
  );
}

export default SubmissionCodeView; 