import React from "react";
import "./Footer.css";
import { 
  Favorite as HeartIcon,
  Code as CodeIcon,
  EmojiEvents as TrophyIcon,
  GitHub as GitHubIcon,
  LinkedIn as LinkedInIcon,
  Twitter as TwitterIcon,
  Email as EmailIcon
} from '@mui/icons-material';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-left">
          <div className="footer-brand">
            <CodeIcon className="footer-logo" />
            <span>© {new Date().getFullYear()} CodeArena. All rights reserved.</span>
          </div>
        </div>
        <div className="footer-center">
          <span>
            Made with <HeartIcon className="heart-icon" /> for coding competitions
          </span>
        </div>
        <div className="footer-right">
          <div className="footer-socials">
            <a href="https://github.com/kuruva-radhakrishna" target="_blank" rel="noopener noreferrer" className="social-link" title="GitHub">
              <GitHubIcon />
            </a>
            <a href="https://www.linkedin.com/in/kuruva-radhakrishna-457167260/" target="_blank" rel="noopener noreferrer" className="social-link" title="LinkedIn">
              <LinkedInIcon />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer; 