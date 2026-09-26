import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import { Link, useNavigate } from 'react-router-dom';
import './Nav.css';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { 
  Code as CodeIcon, 
  EmojiEvents as ContestIcon, 
  Add as AddIcon, 
  Person as PersonIcon, 
  Logout as LogoutIcon,
  Login as LoginIcon,
  PersonAdd as SignUpIcon,
  Home as HomeIcon,
  SmartToy as AIIcon
} from '@mui/icons-material';

function Nav() {
  const { user, setUser } = useAuth();
  const navigate = useNavigate();
  const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

  const handleLogout = async () => {
    try {
      await axios.post(`${BACKEND_URL}/logout`, {}, { withCredentials: true });
      setUser(null);
      navigate('/login');
    } catch (err) {
      // handle error
    }
  };

  return (
    <AppBar position="static" className="nav-appbar" mb="5" component="nav" elevation={0}>
      <Toolbar>
        <div className="navbar-left">
          <Link to="/" className="navbar-logo" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', marginRight: 32 }}>
            <img src="/logo.png" alt="CodeArena Logo" style={{ height: 40, marginRight: 12, verticalAlign: 'middle' }} />
            <span className="ca-navbar-brand" style={{ 
              fontWeight: 700, 
              fontSize: '1.4rem', 
              color: '#222', 
              letterSpacing: '0.5px', 
              fontFamily: 'Segoe UI, Arial, sans-serif'
            }}>
              <span style={{ color: '#fff' }}>Code</span>
              <span style={{ color: '#ffa116' }}>Arena</span>
            </span>
          </Link>
        </div>
        <div style={{ flexGrow: 1 }} />
        {user && (
          <>
            <Button 
              color="inherit" 
              component={Link} 
              to="/problems" 
              className="nav-link"
              startIcon={<CodeIcon />}
            >
              Problems
            </Button>
            <Button 
              color="inherit" 
              component={Link} 
              to="/contests" 
              className="nav-link"
              startIcon={<ContestIcon />}
            >
              Contests
            </Button>
            {user.role === 'admin' && (
              <>
                <Button 
                  color="inherit" 
                  component={Link} 
                  to="/problems/new" 
                  className="nav-link nav-admin"
                  startIcon={<AddIcon />}
                >
                  Create Problem
                </Button>
                <Button 
                  color="inherit" 
                  component={Link} 
                  to="/contests/new" 
                  className="nav-link nav-admin"
                  startIcon={<ContestIcon />}
                >
                  Create Contest
                </Button>
              </>
            )}
            <Link to="/profile" className="nav-link nav-profile-circle" style={{ textDecoration: 'none' }}>
              {(
                (user && user.firstname?.[0] || '').toUpperCase() +
                (user && user.lastname?.[0] || '').toUpperCase()
              )}
            </Link>
            <Button 
              color="inherit" 
              onClick={handleLogout} 
              className="nav-link"
              startIcon={<LogoutIcon />}
            >
              Logout
            </Button>
          </>
        )}
        {!user && (
          <>
            <Button 
              color="inherit" 
              component={Link} 
              to="/login" 
              className="nav-link"
              startIcon={<LoginIcon />}
            >
              Login
            </Button>
            <Button 
              color="inherit" 
              component={Link} 
              to="/signup" 
              className="nav-link"
              startIcon={<SignUpIcon />}
            >
              Sign Up
            </Button>
          </>
        )}
      </Toolbar>
    </AppBar>
  );
}

export default Nav; 