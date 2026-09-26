import axios from "axios";
import { useState, useEffect } from "react";
import { useParams, Link, Outlet } from "react-router-dom";
import "./ContestView.css";
import ReactMarkdown from 'react-markdown';
import CircularProgress from '@mui/material/CircularProgress';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

function ContestView({ contestId }) {
    const [contest, setContest] = useState(null);
    const [status, setStatus] = useState("loading"); // 'future', 'ongoing', 'past', 'loading', 'notlive'
    const [now, setNow] = useState(new Date());
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchContest() {
            try {
                const res = await axios.get(
                    `${BACKEND_URL}/contests/${contestId}`,
                    { withCredentials: true }
                );
                setContest(res.data);
                const start = new Date(res.data.startTime);
                const end = new Date(res.data.endTime);
                if (now < start) setStatus("future");
                else if (now >= start && now < end) setStatus("ongoing");
                else setStatus("past");
            } catch (err) {
                if (err.response && err.response.status === 400) {
                    setStatus("notlive");
                } else {
                    setError("Failed to load contest.");
                }
            }
        }
        fetchContest();
        const timer = setInterval(() => setNow(new Date()), 1000);
        return () => clearInterval(timer);
    }, [contestId]);

    if (status === "loading") return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}><CircularProgress size={60} thickness={5} /></div>;
    if (error) return <div style={{ color: "red" }}>{error}</div>;
    if (status === "notlive" || status === "future")
        return (
            <div className="contest-container contest-future">
                <h2 className="section-title">
                    <span role="img" aria-label="calendar">
                        📅
                    </span>{" "}
                    {contest ? contest.contestTitle : "Contest"}
                </h2>
                {contest && <div style={{margin: '24px 0'}}><ContestRulesSection contest={contest} forceShowDescription={true} /></div>}
                {contest && <Countdown to={contest.startTime} label="Starts in" />}
                <p style={{ marginTop: 32, fontSize: '1.18rem', fontWeight: 500, color: '#222' }}>Contest has not started yet.</p>
            </div>
        );
    if (!contest) return null;
    if (status === "ongoing")
        return (
            <div className="contest-container contest-ongoing">
                <h2 className="section-title">
                    <span role="img" aria-label="trophy">
                        🏆
                    </span>{" "}
                    {contest.contestTitle}
                </h2>
                <ContestRulesSection contest={contest} />
                <Countdown to={contest.endTime} label="Ends in" />
                <ProblemsSection contest={contest} />
                <LeaderboardSection contestId={contestId} />
            </div>
        );
    if (status === "past")
        return (
            <div className="contest-container contest-past">
                <h2 className="section-title">
                    <span role="img" aria-label="trophy">
                        🏆
                    </span>{" "}
                    {contest.contestTitle}
                </h2>
                <ContestRulesSection contest={contest} />
                <p>Contest is over.</p>
                <ProblemsSection contest={contest} />
                <LeaderboardSection contestId={contestId} />
            </div>
        );
}

function Countdown({ to, label }) {
    const [time, setTime] = useState("");
    useEffect(() => {
        const interval = setInterval(() => {
            const diff = new Date(to) - new Date();
            if (diff <= 0) setTime("00:00:00");
            else {
                const h = String(Math.floor(diff / 3600000)).padStart(2, "0");
                const m = String(Math.floor((diff % 3600000) / 60000)).padStart(2, "0");
                const s = String(Math.floor((diff % 60000) / 1000)).padStart(2, "0");
                setTime(`${h}:${m}:${s}`);
            }
        }, 1000);
        return () => clearInterval(interval);
    }, [to]);
    return (
        <div className="countdown">
            {label}: {time}
        </div>
    );
}

function ProblemsSection({ contest }) {
    return (
        <div className="ongoing-problems">
            <h3 className="section-title">
                <span role="img" aria-label="problem">
                    📝
                </span>{" "}
                Problems
            </h3>
            <table className="problems-table">
                <thead>
                    <tr>
                        <th>S.No</th>
                        <th>Problem Name</th>
                        <th>Points</th>
                    </tr>
                </thead>
                <tbody>
                    {contest.problems.map((p, idx) => (
                        <tr key={p.problem_id._id || p.problem_id}>
                            <td>{idx + 1}</td>
                            <td>
                                <Link
                                    className="problem-link"
                                    to={`/contests/${contest._id}/problem/${p.problem_id._id || p.problem_id}/description`}
                                    state={{ contestId: contest._id }}
                                >
                                    {p.problem_id.problemName ||
                                        `Problem ${String.fromCharCode(65 + idx)}`}
                                </Link>
                            </td>
                            <td>
                                <span className="badge">{p.points} pts</span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function LeaderboardSection({ contestId }) {
    const [leaderboard, setLeaderboard] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function fetchLeaderboard() {
            try {
                const res = await axios.get(
                    `${BACKEND_URL}/contests/${contestId}/leaderboard`,
                    { withCredentials: true }
                );
                setLeaderboard(res.data);
            } catch (err) {
                setLeaderboard([]);
            }
            setLoading(false);
        }
        fetchLeaderboard();
    }, [contestId]);

    if (loading) return <div>Loading leaderboard...</div>;
    if (!leaderboard || leaderboard.length === 0)
        return <div>No leaderboard data.</div>;

    const problemCount = leaderboard[0]?.problems.length || 0;

    return (
        <>
            <h3 className="section-title">
                <span role="img" aria-label="leaderboard">📊</span>{" "}
                Leaderboard
            </h3>

            <div className="contest-leaderboard">
                <table>
                    <thead>
                        <tr>
                            <th className="rank-cell">Rank</th>
                            <th>User</th>
                            <th>Total Points</th>
                            {[...Array(problemCount)].map((_, i) => (
                                <th key={i}>{`Q${i + 1}`}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {leaderboard.map((entry, idx) => (
                            <tr
                                key={entry.user._id}
                                className={`top-${idx + 1 <= 3 ? idx + 1 : ""}`}
                            >
                                <td className="rank-cell">
                                    <span className="rank-stack">
                                        {idx === 0 && (
                                            <span className="trophy" role="img" aria-label="gold">🏆</span>
                                        )}
                                        {idx === 1 && (
                                            <span className="trophy" role="img" aria-label="silver">🥈</span>
                                        )}
                                        {idx === 2 && (
                                            <span className="trophy" role="img" aria-label="bronze">🥉</span>
                                        )}
                                        <span className="rank-number">{idx + 1}</span>
                                    </span>
                                </td>

                                <td>
                                    {entry.user.firstname} {entry.user.lastname}
                                </td>

                                <td>{entry.totalPoints}</td>

                                

                                {entry.problems.map((submission, i) => (
                                    <td key={i}>
                                        {submission?.verdict === "Accepted" ? (
                                            <span className="badge bg-success">✅</span>
                                        ) : submission ? (
                                            <span className="badge bg-danger">❌</span>
                                        ) : (
                                            ""
                                        )}
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </>
    );
}


function ContestRulesSection({ contest, forceShowDescription }) {
    const defaultRules = [
        "Each problem carries the points shown in the table.",
        "You can submit solutions as many times as you like; only your best score counts.",
        "Plagiarism or cheating will result in disqualification.",
        "Leaderboard is updated in real-time.",
        "Partial points may be awarded for partially correct solutions (if supported).",
        "Do not discuss problems publicly until the contest ends.",
        "In case of ties, the user with the earliest last correct submission ranks higher.",
        "If you make multiple correct submissions for a problem, only your last correct submission will be considered for tie-breaking."
    ];
    return (
        <>
            {(forceShowDescription || contest.description) && (
                <div className="contest-description-box" style={{background: '#f8fafc', borderRadius: 10, padding: '18px 22px', margin: '18px 0 24px 0', textAlign: 'left', boxShadow: '0 1px 4px rgba(25,118,210,0.06)'}}>
                    <div style={{fontWeight: 700, fontSize: '1.13rem', marginBottom: 8}}>Description</div>
                    <ReactMarkdown>{contest.description || 'No description provided.'}</ReactMarkdown>
                </div>
            )}
            <div className="contest-rules-box" style={{background: '#f6faff', borderRadius: 10, padding: '18px 22px', textAlign: 'left', boxShadow: '0 1px 4px rgba(25,118,210,0.06)'}}>
                <h3 className="section-title" style={{textAlign: 'left', marginTop: 0}}>
                    <span role="img" aria-label="rules">📜</span> Contest Rules & Info
                </h3>
                <ul className="contest-rules-list" style={{textAlign: 'left'}}>
                    {(contest.rules && contest.rules.length > 0 ? contest.rules : defaultRules).map((rule, idx) => (
                        <li key={idx}>{rule}</li>
                    ))}
                </ul>
                <div className="leaderboard-info" style={{textAlign: 'left'}}>
                    <strong>Leaderboard:</strong> Rankings are based on total points. In case of a tie, the user with the earliest last correct submission is ranked higher.
                </div>
            </div>
        </>
    );
}

export default ContestView;
