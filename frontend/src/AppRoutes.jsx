import { Routes, Route, Navigate, useLocation, useParams } from 'react-router-dom';
import SignUp from './components/Auth/SignUp';
import Login from './components/Auth/Login';
import Problems from './components/Problems/Problems';
import Problem from './components/Problems/ProblemView';
import ProblemDescription from './components/Problems/ProblemDescription';
import ProblemSubmissions from './components/Problems/ProblemSubmissions';
import ProblemDiscussion from './components/Problems/ProblemDiscussion';
import Contests from './components/Contests/Contests';
import ContestLeaderBoard from './components/Contests/ContestLeaderBoard';
import ContestView from './components/Contests/ContestView';
import ContestDiscussions from './components/Contests/ContestDiscussions';
import ContestSubmissions from './components/Contests/ContestSubmissions';
import Nav from './components/Nav';
import SubmissionCodeView from './components/Problems/SubmissionCodeView';
import { useAuth } from './contexts/AuthContext';
import Profile from './components/Profile';
import Home from './components/Home';
import NewProblem from './components/Problems/NewProblem';
import EditProblem from './components/Problems/EditProblem';
import NewContest from './components/Contests/NewContest';
import ContestProblemView from './components/Contests/ContestProblemView';
import ContestProblemDescription from './components/Contests/ContestProblemDescription';
import ContestProblemSubmissions from './components/Contests/ContestProblemSubmissions';
import EditContest from './components/Contests/EditContest';
import AIChat from './components/AIChat/AIChat';
import ProtectedRoute from './components/ProtectedRoute';

function AppRoutes() {
  // Always show Nav
  return (
    <>
      <Nav />
      <AIChat />
      <div style={{ position: 'relative', minHeight: '100vh' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/login" element={<Login />} />
          <Route path="/problems" element={
            <ProtectedRoute><Problems /></ProtectedRoute>
          } />
          <Route path="/problems/new" element={
            <ProtectedRoute><NewProblem /></ProtectedRoute>
          } />
          <Route path="/problem/:id/*" element={
            <ProtectedRoute><Problem /></ProtectedRoute>
          }>
            <Route index element={<Navigate to="description" />} />
            <Route path="description" element={<ProblemDescription />} />
            <Route path="submissions" element={<ProblemSubmissions />} />
            <Route path="discussions" element={<ProblemDiscussion />} />
          </Route>
          <Route path="/problems/:id/edit" element={
            <ProtectedRoute><EditProblem /></ProtectedRoute>
          } />
          <Route path='/contests' element={
            <ProtectedRoute><Contests /></ProtectedRoute>
          } />
          <Route path='/contests/:id/*' element={
            <ProtectedRoute><ContestView /></ProtectedRoute>
          } >
            <Route path='leaderboard' element={<ContestLeaderBoard />} />
            <Route path='submissions' element={<ContestSubmissions />} />
            <Route path='discussions' element={<ContestDiscussions />} />
          </Route>
          <Route path="/submission/:id" element={
            <ProtectedRoute><SubmissionCodeView /></ProtectedRoute>
          } />
          <Route path='/profile' element={
            <ProtectedRoute><Profile /></ProtectedRoute>
          } />
          <Route path='/contests/:id' element={
            <ProtectedRoute><ContestViewWrapper /></ProtectedRoute>
          } />
          <Route path="/contests/:contestId/problem/:problemId/*" element={
            <ProtectedRoute><ContestProblemView /></ProtectedRoute>
          }>
            <Route path="description" element={<ContestProblemDescription />} />
            <Route path="submissions" element={<ContestProblemSubmissions />} />
          </Route>
          <Route path="/problem/:problemId/*" element={
            <ProtectedRoute><Problem /></ProtectedRoute>
          } />
          <Route path="/contests/new" element={
            <ProtectedRoute><NewContest /></ProtectedRoute>
          } />
          <Route path="/contests/:id/edit" element={
            <ProtectedRoute><EditContest /></ProtectedRoute>
          } />
        </Routes>
      </div>
    </>
  );
}

function ContestViewWrapper() {
  const { id } = useParams();
  return <ContestView contestId={id} />;
}

export default AppRoutes; 