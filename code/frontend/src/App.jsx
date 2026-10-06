import { Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { useContext } from 'react';
import { AuthProvider, AuthContext } from './context/AuthContext';
import Login from './pages/Login';
import Catalog from './pages/Catalog';
import Navbar from './components/Navbar';
import Profile from './pages/Profile';

// อัปเกรด Guard ให้รับพารามิเตอร์ allowedRoles เพื่อเช็กสิทธิ์
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, isLoading } = useContext(AuthContext);
  
  if (isLoading) return null; 
  
  // 1. ถ้าไม่ได้ล็อกอิน เตะกลับไปหน้า Login
  if (!user) return <Navigate to="/login" replace />; 
  
  // 2. ถ้ามีการระบุสิทธิ์ที่เข้าได้ และ Role ของผู้ใช้ไม่ได้อยู่ในนั้น ให้เตะกลับไปหน้าแรก (แคตตาล็อก)
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />; 
  }
  
  return children;
};

function AppContent() {
  const location = useLocation();
  const isLoginPage = location.pathname === '/login';

  return (
    <div className="min-h-screen bg-gray-50">
      {!isLoginPage && <Navbar />}
      
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Catalog />} />
        
        {/* หน้า Profile เข้าได้ทุกคนที่ล็อกอินแล้ว (ส่งแค่ ProtectedRoute เปล่าๆ) */}
        <Route 
          path="/profile" 
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          } 
        />

        {/* 
          เตรียมโครงสร้าง Route สำหรับเฟส 3: 
          หน้า Admin/Librarian บังคับว่าต้องมี Role ตรงตามที่กำหนดเท่านั้นถึงจะเข้าได้ 
        */}
        <Route 
          path="/admin/books" 
          element={
            <ProtectedRoute allowedRoles={['LIBRARIAN', 'ADMIN']}>
              <div className="p-10 text-center font-bold">กำลังสร้างหน้าจัดการหนังสือ...</div>
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/admin/loans" 
          element={
            <ProtectedRoute allowedRoles={['LIBRARIAN', 'ADMIN']}>
              <div className="p-10 text-center font-bold">กำลังสร้างหน้าจัดการใบยืม...</div>
            </ProtectedRoute>
          } 
        />
      </Routes>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}