import './ProblemDiscussion.css';
import { useEffect, useState } from 'react';
import axios from 'axios';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

function ProblemDiscussion({ problemId }) {
    const [discussions, setDiscussions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [comment, setComment] = useState('');
    const [posting, setPosting] = useState(false);
    const [postError, setPostError] = useState('');

    const fetchDiscussions = async () => {
        setLoading(true);
        setError('');
        try {
            const res = await axios.get(`${BACKEND_URL}/problems/${problemId}/discussions`, { withCredentials: true });
            setDiscussions(res.data);
        } catch (err) {
            setError('Failed to load discussions.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (problemId) fetchDiscussions();
        // eslint-disable-next-line
    }, [problemId]);

    const handlePost = async (e) => {
        e.preventDefault();
        if (!comment.trim()) return;
        setPosting(true);
        setPostError('');
        try {
            await axios.post(`${BACKEND_URL}/problems/${problemId}/discussions`, { comment }, { withCredentials: true });
            setComment('');
            fetchDiscussions();
        } catch (err) {
            setPostError('Failed to post discussion.');
        } finally {
            setPosting(false);
        }
    };

    return (
        <div className="problem-discussion">
            <h2>Problem Discussions</h2>
            <form className="discussion-form" onSubmit={handlePost}>
                <textarea
                    value={comment}
                    onChange={e => setComment(e.target.value)}
                    placeholder="Add your comment..."
                    rows={3}
                    disabled={posting}
                />
                <button type="submit" disabled={posting || !comment.trim()}>
                    {posting ? 'Posting...' : 'Post'}
                </button>
                {postError && <div className="error-msg">{postError}</div>}
            </form>
            <div className="discussion-list">
                {loading ? (
                    <p>Loading discussions...</p>
                ) : error ? (
                    <p className="error-msg">{error}</p>
                ) : discussions.length === 0 ? (
                    <p>No discussions yet. Be the first to comment!</p>
                ) : (
                    discussions.map((d, i) => (
                        <div className="comment" key={i}>
                            <div className="comment-user">
                                <span className="user-avatar">
                                    {d.user?.firstname?.[0]?.toUpperCase()}{d.user?.lastname?.[0]?.toUpperCase()}
                                </span>
                                <span className="user-name">{d.user?.firstname} {d.user?.lastname}</span>
                                <span className="user-email">({d.user?.email})</span>
                            </div>
                            <div className="comment-content">{d.comment}</div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default ProblemDiscussion;