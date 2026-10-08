import { createContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';

// สร้าง Context เป็นศูนย์กลางข้อมูล
export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // ตรวจสอบ Token ในกระเป๋าทุกครั้งที่โหลดแอปพลิเคชัน
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decoded = jwtDecode(token);
        setUser({
          id: decoded.id || decoded.userId || decoded.sub,
          role: decoded.role,
          username: decoded.sub // Spring Security มักเก็บ username ไว้ใน sub
        });
      } catch (error) {
        console.error("Token ไม่ถูกต้อง:", error);
        localStorage.removeItem('token');
      }
    }
    setIsLoading(false);
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
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};