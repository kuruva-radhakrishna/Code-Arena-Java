import { useEffect, useState } from "react";
import { useParams, useNavigate, Navigate, Link, Routes, Route } from "react-router-dom";
import axios from "axios";
import CodeEditor from '../Problems/CodeEditor';
import InputOutputConsole from '../Problems/InputOutputConsole';
import Verdict from '../Problems/Verdict';
import ProblemDescription from '../Problems/ProblemDescription';
import ContestProblemSubmissions from './ContestProblemSubmissions';
import Box from '@mui/material/Box';
import ReactMarkdown from 'react-markdown';
import '../../App.css';
import ContestProblemDescription from './ContestProblemDescription';
import CircularProgress from '@mui/material/CircularProgress';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
const COMPILER_URL = import.meta.env.VITE_COMPILER_URL;

function ContestProblemView() {
    const { contestId, problemId } = useParams();
    const [contest, setContest] = useState(null);
    const [contestStatus, setContestStatus] = useState("loading");
    const [problem, setProblem] = useState();
    const [problemError, setProblemError] = useState(false);
    const [language, setLanguage] = useState('cpp');
    const [code, setCode] = useState('');
    const [input, setInput] = useState('');
    const [output, setOutput] = useState('');
    const [verdicts, setVerdicts] = useState([]);
    const [submissions, setSubmissions] = useState([]);
    const [timerString, setTimerString] = useState("");
    const [runLoading, setRunLoading] = useState(false);
    const [submitLoading, setSubmitLoading] = useState(false);

    useEffect(() => {
        switch (language) {
            case "c++":
                setCode(`#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // your code here\n    return 0;\n}`);
                break;
            case "python":
                setCode(`def main():\n    # your code here\n    pass\n\nif __name__ == "__main__":\n    main()`);
                break;
            case "java":
                setCode(`public class Main {\n    public static void main(String[] args) {\n        // your code here\n    }\n}`);
                break;
            default:
                setCode(`#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // your code here\n    return 0;\n}`);
        }
    }, [language]);

    useEffect(() => {
        const fetchContest = async function () {
            try {
                const res = await axios.get(`${BACKEND_URL}/contests/${contestId}`, { withCredentials: true });
                setContest(res.data);
                const start = new Date(res.data.startTime);
                const end = new Date(res.data.endTime);
                const now = new Date();
                if (now >= start && now < end) {
                    setContestStatus("ongoing");
                }
                else setContestStatus("not-ongoing");
            } catch (err) {
                setContestStatus("not-ongoing");
            }
        }
        fetchContest();
    }, [contestId]);

    useEffect(() => {
        if (contestStatus !== "ongoing") {
            return;
        }
        const fetchProblem = async function () {
            try {
                const result = await axios.get(`${BACKEND_URL}/problems/${problemId}`, { withCredentials: true });
                if (result.data) {
                    setProblem(result.data);
                    setProblemError(false);
                } else {
                    setProblem(null);
                    setProblemError(true);
                }
            } catch (err) {
                setProblem(null);
                setProblemError(true);
            }
        }
        fetchProblem();
        fetchSubmissions();
    }, [problemId, contestStatus]);

    const fetchSubmissions = async () => {
        try {
            const result = await axios.get(`${BACKEND_URL}/submissions/${problemId}`, { withCredentials: true });
            let filtered = result.data;
            if (contestId && contestStatus === "ongoing") {
                filtered = filtered.filter(
                    (sub) => sub.contest_id && sub.contest_id === contestId
                );
            }
            setSubmissions(filtered);
        } catch (error) {
            setSubmissions([]);
        }
    };

    useEffect(() => {
        if (contestStatus === "ongoing" && contest && contest.endTime) {
            const updateTimer = () => {
                const diff = new Date(contest.endTime) - new Date();
                if (diff <= 0) {
                    setTimerString("00:00:00");
                    setContestStatus("not live");
                }
                else {
                    const h = String(Math.floor(diff / 3600000)).padStart(2, "0");
                    const m = String(Math.floor((diff % 3600000) / 60000)).padStart(2, "0");
                    const s = String(Math.floor((diff % 60000) / 1000)).padStart(2, "0");
                    setTimerString(`${h}:${m}:${s}`);
                }
            };
            updateTimer();
            const interval = setInterval(updateTimer, 1000);
            return () => clearInterval(interval);
        }
    }, [contestStatus, contest]);

    if (contestStatus === "loading") {
        return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}><CircularProgress size={60} thickness={5} /></div>;
    }
    if (contestStatus !== "ongoing") {
        return <Navigate to={`/problem/${problemId}/description`} replace />;
    }
    if (!problem) {
        return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}><CircularProgress size={60} thickness={5} /></div>;
    }

    const handleRun = async () => {
        setRunLoading(true);
        try {
            const result = await axios.post(`${COMPILER_URL}/run`, {
                language,
                code,
                input
            }, { withCredentials: true });
            if (result.data.output) {
                setOutput(result.data.output.output);
            }
            if (result.data.errorType) {
                setOutput(result.data.errorType);
            }
        } catch (error) {
            setOutput('Run failed.');
        }
        setRunLoading(false);
    };
    const handleSubmit = async () => {
        setSubmitLoading(true);
        try {
            const url = `${BACKEND_URL}/contests/submission/${contestId}/${problemId}`;
            const payload = {
                submission: {
                    language,
                    code,
                }
            };
            const result = await axios.post(url, payload, { withCredentials: true });
            setVerdicts(result.data);
            fetchSubmissions();
        } catch (error) {
            console.log(error.message);
        }
        setSubmitLoading(false);
    };

    return (
        <div className="problem-view" style={{ position: 'relative' }}>
            <div className="contest-timer-topright">
                <span role="img" aria-label="stopwatch" className="timer-icon">⏱️</span>
                <span className="timer-value">{timerString}</span>
                <span className="timer-label">left</span>
            </div>
            <div className="row" style={{ display: "flex" }}>
                <div className="problem" style={{ width: "50%" }}>
                    <div style={{ marginBottom: "1rem" }}>
                        <Link to={`/contests/${contestId}/problem/${problemId}/description`}><button>Description</button></Link>
                        <Link to={`/contests/${contestId}/problem/${problemId}/submissions`}><button>Submissions</button></Link>
                    </div>
                    <Routes>
                        <Route path="description" element={<ContestProblemDescription problem={problem} />} />
                        <Route path="submissions" element={<ContestProblemSubmissions submissions={submissions} refreshSubmissions={fetchSubmissions} />} />
                    </Routes>
                </div>
                <div className="solution" style={{ width: "50%" }}>
                    <select
                        name="language"
                        value={language}
                        onChange={e => setLanguage(e.target.value)}
                        className="language-select"
                    >
                        <option value="cpp">C++</option>
                        <option value="java">Java</option>
                        <option value="python">Python</option>
                    </select>
                    <CodeEditor value={code} onChange={setCode} language={language} />
                    <Box display="flex" gap={2} mt={2} justifyContent="space-between">
                        <div style={{ display: 'flex', gap: 16 }}>
                            <button onClick={handleRun} className="run-btn" disabled={runLoading}>
                                {runLoading ? "Running..." : "▶️ Run"}
                            </button>
                            <button onClick={handleSubmit} className="submit-btn" disabled={submitLoading}>
                                {submitLoading ? "Submitting..." : "📤 Submit"}
                            </button>
                        </div>
                    </Box>
                    <InputOutputConsole inputValue={input} onInputChange={e => setInput(e.target.value)} outputValue={output} isOutput={true} />
                    <Verdict verdicts={verdicts} />
                </div>
            </div>
        </div>
    );
}

export default ContestProblemView; 