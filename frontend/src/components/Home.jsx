import React from 'react';
import { Link } from 'react-router-dom';
import './Home.css';
import { useAuth } from '../contexts/AuthContext';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import CodeIcon from '@mui/icons-material/Code';
import LeaderboardIcon from '@mui/icons-material/Leaderboard';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import ForumIcon from '@mui/icons-material/Forum';
import SchoolIcon from '@mui/icons-material/School';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import StarIcon from '@mui/icons-material/Star';
import { 
  Computer as ComputerIcon,
  Speed as SpeedIcon,
  Group as GroupIcon,
  Lightbulb as LightbulbIcon,
  RocketLaunch as RocketIcon,
  CheckCircle as CheckIcon,
  ArrowForward as ArrowIcon
} from '@mui/icons-material';

const PROJECT_NAME = "CodeArena";

const Home = () => {
  const {user} = useAuth();
  return (
    <div className="oj-home-container">
      <section className="oj-hero-modern">
        <div className="oj-hero-bg-gradient"></div>
        <div className="oj-hero-content">
          <div className="oj-hero-logo">
            <ComputerIcon style={{ fontSize: '4rem', color: '#ffa116', marginBottom: '16px' }} />
          </div>
          <h1>Welcome to <span className="oj-highlight">{PROJECT_NAME}</span></h1>
          <p className="oj-subtitle">
            <LightbulbIcon style={{ marginRight: '8px', fontSize: '1.2rem', verticalAlign: 'middle' }} />
            Sharpen your coding skills, compete in contests, and join a vibrant developer community.
          </p>
          <div className="oj-hero-actions">
            {!user && <Link to="/signup" className="oj-btn oj-btn-primary">
              <RocketIcon style={{ marginRight: '8px' }} />
              Get Started
            </Link>}
            <Link to="/problems" className="oj-btn oj-btn-secondary">
              <CodeIcon style={{ marginRight: '8px' }} />
              Practice Problems
            </Link>
          </div>
        </div>
      </section>
      
      <section className="oj-about">
        <h2>
          <StarIcon style={{ marginRight: '12px', color: '#ffa116' }} />
          About CodeArena
        </h2>
        <p>
          <GroupIcon style={{ marginRight: '8px', fontSize: '1.1rem', verticalAlign: 'middle', color: '#1976d2' }} />
          CodeArena is your all-in-one platform to practice coding, compete in live contests, and grow as a developer. Whether you're a beginner or a pro, you'll find challenges, community, and tools to help you succeed.
        </p>
      </section>
      
      <section className="oj-features-modern">
        <div className="oj-feature-card-modern">
          <div className="oj-feature-icon">
            <CodeIcon style={{ fontSize: '2.5rem', color: '#1976d2' }} />
          </div>
          <h2>Practice Problems</h2>
          <p>Challenge yourself with a wide range of problems in algorithms, data structures, and more.</p>
          <div className="oj-feature-arrow">
            <ArrowIcon style={{ color: '#1976d2', fontSize: '1.2rem' }} />
          </div>
        </div>
        <div className="oj-feature-card-modern">
          <div className="oj-feature-icon">
            <EmojiEventsIcon style={{ fontSize: '2.5rem', color: '#ffa116' }} />
          </div>
          <h2>Contests</h2>
          <p>Participate in live and virtual contests to test your skills against others and climb the leaderboard.</p>
          <div className="oj-feature-arrow">
            <ArrowIcon style={{ color: '#ffa116', fontSize: '1.2rem' }} />
          </div>
        </div>
        <div className="oj-feature-card-modern">
          <div className="oj-feature-icon">
            <LeaderboardIcon style={{ fontSize: '2.5rem', color: '#4caf50' }} />
          </div>
          <h2>Leaderboards</h2>
          <p>Track your progress and see how you rank among other coders in the community.</p>
          <div className="oj-feature-arrow">
            <ArrowIcon style={{ color: '#4caf50', fontSize: '1.2rem' }} />
          </div>
        </div>
        <div className="oj-feature-card-modern">
          <div className="oj-feature-icon">
            <SmartToyIcon style={{ fontSize: '2.5rem', color: '#9c27b0' }} />
          </div>
          <h2>AI Integration</h2>
          <p>
            Get instant help from our floating AI chat assistant. Ask about algorithms, interview prep, graphs, or get code review—anytime, on any page.
          </p>
          <div className="oj-feature-arrow">
            <ArrowIcon style={{ color: '#9c27b0', fontSize: '1.2rem' }} />
          </div>
        </div>
        <div className="oj-feature-card-modern">
          <div className="oj-feature-icon">
            <ForumIcon style={{ fontSize: '2.5rem', color: '#ff5722' }} />
          </div>
          <h2>Discussions</h2>
          <p>Engage with the community, ask questions, and share solutions in our discussion forums.</p>
          <div className="oj-feature-arrow">
            <ArrowIcon style={{ color: '#ff5722', fontSize: '1.2rem' }} />
          </div>
        </div>
        <div className="oj-feature-card-modern">
          <div className="oj-feature-icon">
            <TrendingUpIcon style={{ fontSize: '2.5rem', color: '#00bcd4' }} />
          </div>
          <h2>Progress Tracking</h2>
          <p>Monitor your learning journey with detailed analytics and personalized recommendations.</p>
          <div className="oj-feature-arrow">
            <ArrowIcon style={{ color: '#00bcd4', fontSize: '1.2rem' }} />
          </div>
        </div>
      </section>
      
      {!user && <section className="oj-get-started-modern">
        <h2>
          <SchoolIcon style={{ marginRight: '12px', color: '#ffa116' }} />
          Ready to begin?
        </h2>
        <p>
          <CheckIcon style={{ marginRight: '8px', fontSize: '1.1rem', verticalAlign: 'middle', color: '#4caf50' }} />
          Sign up now and start your journey to becoming a better programmer!
        </p>
        <Link to="/signup" className="oj-btn oj-btn-primary">
          <RocketIcon style={{ marginRight: '8px' }} />
          Sign Up
        </Link>
      </section>}
      
      {user && <section className="oj-welcome-back">
        <h2>
          <SpeedIcon style={{ marginRight: '12px', color: '#4caf50' }} />
          Welcome back, {user.firstname}!
        </h2>
        <p>Ready to continue your coding journey? Jump back into practice or explore new challenges.</p>
        <div className="oj-welcome-actions">
          <Link to="/problems" className="oj-btn oj-btn-primary">
            <CodeIcon style={{ marginRight: '8px' }} />
            Continue Practice
          </Link>
          <Link to="/contests" className="oj-btn oj-btn-secondary">
            <EmojiEventsIcon style={{ marginRight: '8px' }} />
            Join Contests
          </Link>
        </div>
      </section>}
    </div>
  );
};

export default Home; 