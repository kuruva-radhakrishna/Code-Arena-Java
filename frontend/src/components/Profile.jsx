import { useEffect, useState } from "react";
import axios from 'axios';
import { useAuth } from "../contexts/AuthContext";
import { Card, CardContent, Typography, Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Avatar, Divider, Button } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import './Profile.css';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import Tooltip from '@mui/material/Tooltip';
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

function Profile() {
    const { user } = useAuth();
    const [profileData, setProfileData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [deleteMsg, setDeleteMsg] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                setLoading(true);
                const res = await axios.get(`${BACKEND_URL}/api/profile/summary`, { withCredentials: true });
                console.log(res.data);
                setProfileData(res.data);
            } catch (error) {
                // handle error
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    // Calculate badges
    const solvedTotal = profileData?.solvedStats?.total || 0;
    const bestRank = profileData?.attendedContests?.length > 0 ? Math.min(...profileData.attendedContests.map(c => c.userStats?.rank || 9999)) : null;
    const isNewUser = profileData?.user && profileData.user.createdAt && (new Date() - new Date(profileData.user.createdAt)) < 7 * 24 * 60 * 60 * 1000;
    const has100Solved = solvedTotal >= 100;
    if(profileData && profileData.submissionsByUser){
        profileData.submissionsByUser.sort((s1,s2)=>s2.createdAt-s1.createdAt);
    }

    if (!profileData || loading) {
        return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}><CircularProgress size={60} thickness={5} /></div>;
    }

    const handleDeleteContest = async (contestId) => {
        if (!window.confirm('Are you sure you want to delete this contest? This action cannot be undone.')) return;
        try {
            await axios.delete(`${BACKEND_URL}/contests/${contestId}`, { withCredentials: true });
            setDeleteMsg('Contest deleted successfully.');
            // Optionally refresh profile data
            setProfileData(prev => ({
                ...prev,
                createdContests: prev.createdContests.filter(c => c._id !== contestId)
            }));
        } catch (error) {
            setDeleteMsg('Failed to delete contest.');
            console.error(error);
        }
    };

    return (
        <Box className="profile-root">
            <div className="profile-banner"></div>
            <Card className="profile-header-card">
                <Avatar className="profile-avatar">
                    {(
                        (profileData.user.firstname?.[0] || '').toUpperCase() +
                        (profileData.user.lastname?.[0] || '').toUpperCase()
                    )}
                </Avatar>
                <Box className="profile-header-info">
                    <Typography variant="h4" fontWeight={700}>
                        {profileData.user.firstname} {profileData.user.lastname}
                        {isNewUser && <span className="profile-badge">New</span>}
                    </Typography>
                    <Typography variant="subtitle1" color="text.secondary">{profileData.user.email}</Typography>
                </Box>
            </Card>
            {/* Row: Solved Problems + Participated Contests */}
            <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap', mb: 3, flexDirection: { xs: 'column', md: 'row' } }}>
                {/* Solved Problems Section */}
                <Card className="profile-section-card" sx={{ flex: 1, mb: 0 }}>
                    <Typography className="profile-section-title">Solved Problems</Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 4 }}>
                        {['easy', 'medium', 'hard'].map((level, idx) => {
                            const colors = ['#43a047', '#ffb300', '#e53935'];
                            const labelColors = ['#43a047', '#ffb300', '#e53935'];
                            const labels = ['Easy', 'Medium', 'Hard'];
                            const solved = profileData.solvedStats ? profileData.solvedStats[level] : 0;
                            const total = profileData.problemTotals ? profileData.problemTotals[level] : 0;
                            const percent = total > 0 ? (solved / total) * 100 : 0;
                            return (
                                <Box
                                    key={labels[idx]}
                                    textAlign="center"
                                    sx={{
                                        width: 120,
                                        height: 130,
                                        display: 'flex',
                                        flexDirection: 'column',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                    }}
                                >
                                    <Typography
                                        variant="subtitle2"
                                        sx={{
                                            color: labelColors[idx],
                                            fontWeight: 700,
                                            fontSize: '1.25rem',
                                            mb: 1,
                                            letterSpacing: 0.2,
                                        }}
                                    >
                                        {labels[idx]}
                                    </Typography>
                                    <Box sx={{ position: 'relative', width: 90, height: 90, mb: 1 }}>
                                        <CircularProgress
                                            variant="determinate"
                                            value={100}
                                            size={90}
                                            thickness={6}
                                            sx={{ color: '#e0e0e0', position: 'absolute', left: 0, top: 0 }}
                                        />
                                        <CircularProgress
                                            variant="determinate"
                                            value={percent}
                                            size={90}
                                            thickness={6}
                                            sx={{ color: colors[idx], position: 'absolute', left: 0, top: 0 }}
                                        />
                                        <Box sx={{
                                            position: 'absolute',
                                            left: 0,
                                            top: 0,
                                            width: 90,
                                            height: 90,
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            pointerEvents: 'none',
                                        }}>
                                            <Typography
                                                variant="h5"
                                                sx={{ fontWeight: 900, color: '#222', fontSize: '1.5rem', letterSpacing: 0 }}
                                            >
                                                {solved}/{total}
                                            </Typography>
                                        </Box>
                                    </Box>
                                </Box>
                            );
                        })}
                    </Box>
                </Card>
                {/* Participated Contests Section */}
                {profileData.attendedContests && profileData.attendedContests.length > 0 && (
                    <Card className="profile-section-card" sx={{ flex: 1, mb: 0 }}>
                        <Typography className="profile-section-title">Participated Contests</Typography>
                        <TableContainer className="profile-table-container" component={Paper}>
                            <Table className="profile-table">
                                <TableHead>
                                    <TableRow>
                                        <TableCell>S.No</TableCell>
                                        <TableCell>Contest Name</TableCell>
                                        <TableCell>Date</TableCell>
                                        <TableCell>Rank</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {profileData.attendedContests.map((contest, idx) => (
                                        <TableRow key={contest._id}>
                                            <TableCell>{idx + 1}</TableCell>
                                            <TableCell>
                                                <Link to={`/contests/${contest._id}`} style={{ textDecoration: 'none', color: '#1976d2' }}>{contest.contestTitle}</Link>
                                            </TableCell>
                                            <TableCell>{formatFormalDate(contest.startTime)}</TableCell>
                                            <TableCell>{contest.userStats ? contest.userStats.rank : '-'}</TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Card>
                )}
            </Box>
            {/* Contests Created By User Section */}
            {profileData.createdContests && profileData.createdContests.length > 0 && (
                <Card className="profile-section-card">
                    <Typography className="profile-section-title">Contests You Created</Typography>
                    <TableContainer className="profile-table-container" component={Paper}>
                        <Table className="profile-table">
                            <TableHead>
                                <TableRow>
                                    <TableCell>S.No</TableCell>
                                    <TableCell>Contest Title</TableCell>
                                    <TableCell>Start Time</TableCell>
                                    <TableCell>End Time</TableCell>
                                    <TableCell>Problems</TableCell>
                                    <TableCell>Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {profileData.createdContests.map((contest, idx) => {
                                    const now = new Date();
                                    const start = new Date(contest.startTime);
                                    const canEdit = start > now;
                                    return (
                                        <TableRow key={contest._id}>
                                            <TableCell>{idx + 1}</TableCell>
                                            <TableCell>
                                                <Link to={`/contests/${contest._id}`} style={{ textDecoration: 'none', color: '#1976d2' }}>{contest.contestTitle}</Link>
                                            </TableCell>
                                            <TableCell>{formatFormalDate(contest.startTime)}</TableCell>
                                            <TableCell>{formatFormalDate(contest.endTime)}</TableCell>
                                            <TableCell>{contest.problems ? contest.problems.length : 0}</TableCell>
                                            <TableCell>
                                                {canEdit && (
                                                    <Button variant="outlined" size="small" onClick={() => navigate(`/contests/${contest._id}/edit`)} style={{ minWidth: 0, padding: '2px 10px', marginRight: 6, borderColor: '#ffa116', color: '#ffa116', fontWeight: 600, background: 'linear-gradient(135deg, #fffbe6 0%, #fff3e0 100%)' }}>
                                                        Edit
                                                    </Button>
                                                )}
                                                <Button variant="outlined" size="small" color="error" onClick={() => handleDeleteContest(contest._id)} style={{ minWidth: 0, padding: '2px 10px', borderColor: '#f44336', color: '#f44336', fontWeight: 600, background: 'linear-gradient(135deg, #fff0f0 0%, #ffeaea 100%)' }}>
                                                    Delete
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Card>
            )}
            {/* Problems You Created Section */}
            {profileData.user && profileData.user.role === 'admin' && (
                <Card className="profile-section-card">
                    <Typography className="profile-section-title">Problems You Created</Typography>
                    <TableContainer className="profile-table-container" component={Paper}>
                        <Table className="profile-table">
                            <TableHead>
                                <TableRow>
                                    <TableCell>S.No</TableCell>
                                    <TableCell>Problem Name</TableCell>
                                    <TableCell>Created At</TableCell>
                                    <TableCell>Edit</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {profileData.problemsByUser && profileData.problemsByUser.length > 0 ? (
                                    profileData.problemsByUser.map((problem, idx) => (
                                        <TableRow key={problem._id}>
                                            <TableCell>{idx + 1}</TableCell>
                                            <TableCell>{problem.problemName}</TableCell>
                                            <TableCell>{formatFormalDate(problem.CreatedAt)}</TableCell>
                                            <TableCell>
                                                <Button variant="outlined" size="small" component={Link} to={`/problems/${problem._id}/edit`} style={{ minWidth: 0, padding: '2px 10px', marginRight: 6, borderColor: '#1976d2', color: '#1976d2', fontWeight: 600, background: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)' }}>
                                                    Edit
                                                </Button>
                                                <Button variant="outlined" size="small" color="error" onClick={() => handleDeleteProblem(problem._id)} style={{ minWidth: 0, padding: '2px 10px', borderColor: '#f44336', color: '#f44336', fontWeight: 600, background: 'linear-gradient(135deg, #fff0f0 0%, #ffeaea 100%)' }}>
                                                    Delete
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={4} align="center">No problems created.</TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Card>
            )}
            {/* Submissions Section */}
            <Card className="profile-section-card">
                <Typography className="profile-section-title">Your Submissions</Typography>
                <TableContainer className="profile-table-container" component={Paper}>
                    <Table className="profile-table">
                        <TableHead>
                            <TableRow>
                                <TableCell>S.No</TableCell>
                                <TableCell>Problem Name</TableCell>
                                <TableCell>Language</TableCell>
                                <TableCell>Verdict</TableCell>
                                <TableCell>Submitted At</TableCell>
                                <TableCell>View</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {profileData.submissionsByUser && profileData.submissionsByUser.length > 0 ? (
                                profileData.submissionsByUser.map((submission, index) => {
                                    if (!submission || !submission._id || !submission.problem_id) return null;
                                    return (
                                        <TableRow key={submission._id}>
                                            <TableCell>{index + 1}</TableCell>
                                            <TableCell>
                                                <Link to={`/problem/${submission.problem_id._id || submission.problem_id}/description`} style={{ textDecoration: 'none', color: '#1976d2' }}>
                                                    {submission.problem_id.problemName || submission.problem_id}
                                                </Link>
                                            </TableCell>
                                            <TableCell>{submission.language}</TableCell>
                                            <TableCell>{submission.verdict}</TableCell>
                                            <TableCell>{formatFormalDate(submission.submittedAt)}</TableCell>
                                            <TableCell>
                                                <Button variant="outlined" size="small" component={Link} to={`/submission/${submission._id}`} style={{ minWidth: 0, padding: '2px 10px', fontWeight: 600, borderColor: '#43a047', color: '#43a047', background: 'linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%)' }}>
                                                    View Code
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={6} align="center">No submissions found.</TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Card>
            {deleteMsg && <div style={{ color: deleteMsg.includes('success') ? 'green' : 'red', marginBottom: 8 }}>{deleteMsg}</div>}
        </Box>
    );
}

export default Profile;