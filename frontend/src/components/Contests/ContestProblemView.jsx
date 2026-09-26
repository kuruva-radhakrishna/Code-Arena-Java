import { useEffect, useState } from "react";
import { useParams, Navigate, Link, Routes, Route } from "react-router-dom";
import api from "../../api/client";
import CodeEditor from '../Problems/CodeEditor';
import InputOutputConsole from '../Problems/InputOutputConsole';
import Verdict from '../Problems/Verdict';
import ContestProblemSubmissions from './ContestProblemSubmissions';
import Box from '@mui/material/Box';
import '../../App.css';
import ContestProblemDescription from './ContestProblemDescription';
import CircularProgress from '@mui/material/CircularProgress';

// The editor's language dropdown uses these lowercase values; the backend's
// Language enum is uppercase.
const LANGUAGE_TO_BACKEND = { c: 'C', cpp: 'CPP', java: 'JAVA', python: 'PYTHON' };

function ContestProblemView() {
    const { contestId, problemId } = useParams();
    const [contest, setContest] = useState(null);
    const [contestStatus, setContestStatus] = useState("loading");
    const [problem, setProblem] = useState();
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
                const res = await api.get(`/api/contests/${contestId}`);
                setContest(res.data);
                const start = new Date(res.data.startTime);
                const end = new Date(res.data.endTime);
                const now = new Date();
                if (now >= start && now < end) {
                    setContestStatus("ongoing");
                }
                else setContestStatus("not-ongoing");
            } catch {
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
                const result = await api.get(`/api/problems/${problemId}`);
                setProblem(result.data ?? null);
            } catch {
                setProblem(null);
            }
        }
        fetchProblem();
        fetchSubmissions();
    }, [problemId, contestStatus]);

    const fetchSubmissions = async () => {
        try {
            const result = await api.get(`/api/problems/${problemId}/submissions`);
            let filtered = result.data;
            if (contestId) {
                filtered = filtered.filter((sub) => sub.contestId === contestId);
            }
            setSubmissions(filtered);
        } catch {
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
            const result = await api.post('/api/execute', {
                language: LANGUAGE_TO_BACKEND[language] || language,
                code,
                input
            });
            if (result.data.status === 'SUCCESS') {
                setOutput(result.data.output ?? '');
            } else {
                setOutput(`${result.data.status}: ${result.data.error || ''}`.trim());
            }
        } catch {
            setOutput('Run failed.');
        }
        setRunLoading(false);
    };
    const handleSubmit = async () => {
        setSubmitLoading(true);
        try {
            const url = `/api/contests/${contestId}/problems/${problemId}/submissions`;
            const payload = {
                language: LANGUAGE_TO_BACKEND[language] || language,
                code,
            };
            const result = await api.post(url, payload);
            setVerdicts(result.data.result.testCaseVerdicts);
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