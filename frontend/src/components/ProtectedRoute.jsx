import { useAuth } from '../contexts/AuthContext';
import { Navigate, useLocation } from 'react-router-dom';
import CircularProgress from '@mui/material/CircularProgress';

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '40vh' }}>
      <CircularProgress size={60} thickness={5} />
    </div>
  );

  if (!user) {
    // Optionally, you can render an error page here instead of Navigate
    return <Navigate to="/login" state={{ from: location, error: 'You need to register or login to access this page.' }} replace />;
  }

  return children;
}

export default ProtectedRoute; 