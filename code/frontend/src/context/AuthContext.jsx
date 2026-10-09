import { useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { AuthContext } from './AuthContextValue';

function userFromStoredToken() {
  if (typeof window === 'undefined') return null;

  const token = localStorage.getItem('token');
  if (!token) return null;

  try {
    const decoded = jwtDecode(token);
    if (decoded.exp && decoded.exp * 1000 <= Date.now()) {
      localStorage.removeItem('token');
      return null;
    }

    return {
      id: decoded.id || decoded.userId || decoded.sub,
      role: decoded.role,
      username: decoded.sub
    };
  } catch (error) {
    console.error('Token ไม่ถูกต้อง:', error);
    localStorage.removeItem('token');
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(userFromStoredToken);

  // Keep this tab in sync when another tab logs in or out.
  useEffect(() => {
    const handleStorage = (event) => {
      if (event.key === null || event.key === 'token') {
        setUser(userFromStoredToken());
      }
    };

    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, []);

  // ฟังก์ชันสำหรับเรียกใช้ตอน Login สำเร็จ
  const login = (token) => {
    localStorage.setItem('token', token);
    const decoded = jwtDecode(token);
    setUser({
      id: decoded.id || decoded.userId || decoded.sub,
      role: decoded.role,
      username: decoded.sub
    });
  };

  // ฟังก์ชันสำหรับเรียกใช้ตอน Logout
  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading: false }}>
      {children}
    </AuthContext.Provider>
  );
}
