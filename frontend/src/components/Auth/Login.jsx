import axios from "axios";
import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import InputAdornment from '@mui/material/InputAdornment';
import './Login.css';
import { useAuth } from '../../contexts/AuthContext';
import { 
  Email as EmailIcon, 
  Lock as LockIcon, 
  Login as LoginIcon,
  PersonAdd as SignUpIcon,
  School as SchoolIcon
} from '@mui/icons-material';

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();
    const { setUser } = useAuth();
    const [success, setSuccess] = useState("");

    const handleSubmit = async function (e) {
        e.preventDefault();
        setError("");
        try {
            const result = await axios.post(`${BACKEND_URL}/login`, {
                email,
                password
            }, {
                withCredentials: true
            });
            console.log(result);
            setUser(result.data.user);
            setSuccess("Problems loaded successfully!");
            navigate('/');
        } catch (error) {
            if (error.response && error.response.data && error.response.data.error) {
                setError(error.response.data.error);
            } else {
                setError('Internal Server Error');
            }
        }
    }

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(""), 2000); // 2 seconds
            return () => clearTimeout(timer);
        }
    }, [success]);

    return (
        <Box className="login-bg">
            <Box className="login-card" component="form" onSubmit={handleSubmit}>
                <div className="login-header">
                    <SchoolIcon className="login-logo-icon" />
                    <Typography variant="h4" className="login-title" mb={2} fontWeight={700} align="center">
                        Welcome Back
                    </Typography>
                    <Typography variant="body1" className="login-subtitle" align="center" color="textSecondary">
                        Sign in to continue your coding journey
                    </Typography>
                </div>
                
                <TextField 
                    label="Email" 
                    type="email" 
                    variant="outlined" 
                    fullWidth 
                    margin="normal" 
                    value={email} 
                    onChange={e => setEmail(e.target.value)}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <EmailIcon color="primary" />
                            </InputAdornment>
                        ),
                    }}
                    className="login-input"
                />
                
                <TextField 
                    label="Password" 
                    type="password" 
                    variant="outlined" 
                    fullWidth 
                    margin="normal" 
                    value={password} 
                    onChange={e => setPassword(e.target.value)}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <LockIcon color="primary" />
                            </InputAdornment>
                        ),
                    }}
                    className="login-input"
                />
                
                <Button 
                    variant="contained" 
                    color="primary" 
                    fullWidth 
                    sx={{ 
                        mt: 3, 
                        mb: 2, 
                        py: 1.5, 
                        fontWeight: 600, 
                        fontSize: '1.1rem',
                        borderRadius: '12px',
                        background: 'linear-gradient(135deg, #ffa116 0%, #ff9800 100%)',
                        '&:hover': {
                            background: 'linear-gradient(135deg, #ff9800 0%, #f57c00 100%)',
                            transform: 'translateY(-2px)',
                            boxShadow: '0 8px 24px rgba(255, 161, 22, 0.3)'
                        },
                        transition: 'all 0.3s ease'
                    }} 
                    type="submit"
                    startIcon={<LoginIcon />}
                >
                    Sign In
                </Button>
                
                {error && (
                    <Box className="error-message">
                        <Typography variant="body2" color="error" align="center">
                            {error}
                        </Typography>
                    </Box>
                )}
                
                <Typography variant="body2" align="center" className="login-link">
                    Don't have an account?
                    <Button component={Link} to="/signup" variant="outlined" size="small" style={{ marginLeft: 10, textTransform: 'none', fontWeight: 600, borderRadius: 8, padding: '2px 16px', fontSize: '1rem' }}>
                        Sign Up
                    </Button>
                </Typography>
            </Box>
        </Box>
    );
}

export default Login;