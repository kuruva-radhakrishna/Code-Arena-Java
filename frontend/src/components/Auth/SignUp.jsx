import { useState } from "react";
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import InputAdornment from '@mui/material/InputAdornment';
import GoogleIcon from '@mui/icons-material/Google';
import GitHubIcon from '@mui/icons-material/GitHub';
import FacebookIcon from '@mui/icons-material/Facebook';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import './SignUp.css';
import { useAuth } from "../../contexts/AuthContext";
import { 
  Person as PersonIcon,
  Email as EmailIcon, 
  Lock as LockIcon, 
  PersonAdd as SignUpIcon,
  Login as LoginIcon,
  School as SchoolIcon
} from '@mui/icons-material';

function SignUp() {
    const [firstname , setFirstname] = useState("");
    const [lastname , setLastname] = useState("");
    const [email , setEmail] = useState("");
    const [password , setPassword] = useState(""); 
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const navigate = useNavigate();
    const { setUser } = useAuth();
    const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
    
    const validateUser = () => {
        if (!firstname || firstname.trim().length < 2) return 'First name must be at least 2 characters.';
        if (!lastname || lastname.trim().length < 2) return 'Last name must be at least 2 characters.';
        if (!email || !/^\S+@\S+\.\S+$/.test(email)) return 'A valid email is required.';
        if (!password || password.length < 6) return 'Password must be at least 6 characters.';
        return '';
    };
    
    const handleSubmit = async function(e){
        e.preventDefault();
        setError("");
        setSuccess("");
        const err = validateUser();
        if (err) {
            setError(err);
            return;
        }
        try {
            const result =  await axios.post(`${BACKEND_URL}/register`,{
                firstname : firstname,
                lastname : lastname,
                email : email,
                password : password,
                role : "user"
            }, { withCredentials: true });
            setUser(result.data.user);
            setSuccess('Registration successful! Redirecting...');
            setTimeout(() => navigate('/problems'), 1000);
        } catch (error) {
            if (error.response && error.response.data && error.response.data.message) {
                setError(error.response.data.message);
            } else {
                setError('Internal Server Issue');
            }
        }
    }
    
    return (
        <Box className="signup-bg">
            <Box className="signup-card" component="form" onSubmit={handleSubmit}>
                <div className="signup-header">
                    <SchoolIcon className="signup-logo-icon" />
                    <Typography variant="h4" className="signup-title" mb={2} fontWeight={700} align="center">
                        Join CodeArena
                    </Typography>
                    <Typography variant="body1" className="signup-subtitle" align="center" color="textSecondary">
                        Start your coding journey today
                    </Typography>
                </div>
                
                <TextField 
                    label="First Name" 
                    variant="outlined" 
                    fullWidth 
                    margin="normal" 
                    value={firstname} 
                    onChange={e => setFirstname(e.target.value)}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <PersonIcon color="primary" />
                            </InputAdornment>
                        ),
                    }}
                    className="signup-input"
                />
                
                <TextField 
                    label="Last Name" 
                    variant="outlined" 
                    fullWidth 
                    margin="normal" 
                    value={lastname} 
                    onChange={e => setLastname(e.target.value)}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <PersonIcon color="primary" />
                            </InputAdornment>
                        ),
                    }}
                    className="signup-input"
                />
                
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
                    className="signup-input"
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
                    className="signup-input"
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
                    startIcon={<SignUpIcon />}
                >
                    Create Account
                </Button>
                
                {error && (
                    <Box className="error-message">
                        <Typography variant="body2" color="error" align="center">
                            {error}
                        </Typography>
                    </Box>
                )}
                
                {success && (
                    <Box className="success-message">
                        <Typography variant="body2" color="success" align="center">
                            {success}
                        </Typography>
                    </Box>
                )}
                
                <Typography variant="body2" align="center" className="signup-link" style={{display: 'block', marginTop: 0, padding: 0, background: 'none', boxShadow: 'none'}}>
                    <span style={{ color: '#ffa116', fontWeight: 700, fontSize: '1.05rem', margin: 0, padding: 0, display: 'inline' }}>Already have an account?</span>
                    <Button component={Link} to="/login" variant="outlined" size="small" style={{ marginLeft: 10, textTransform: 'none', fontWeight: 600, borderRadius: 8, padding: '2px 16px', fontSize: '1rem' }}>
                        Login
                    </Button>
                </Typography>
                
                {/*
                <Typography variant="body2" align="center" color="textSecondary" mt={2} mb={1}>
                    or you can sign up with
                </Typography>
                <Box className="signup-socials" display="flex" justifyContent="center" gap={2} mt={1}>
                    <GoogleIcon className="signup-social-icon" />
                    <GitHubIcon className="signup-social-icon" />
                    <FacebookIcon className="signup-social-icon" />
                    <MoreHorizIcon className="signup-social-icon" />
                </Box>
                */}
            </Box>
        </Box>
    );
}

export default SignUp;