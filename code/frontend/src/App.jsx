import { Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { useContext, useState } from 'react';
import { AuthProvider } from './context/AuthContext';
import { AuthContext } from './context/AuthContextValue';
import { LanguageContext } from './context/LanguageContext';
import Login from './pages/Login';
import Catalog from './pages/Catalog';
import Navbar from './components/Navbar';
import Profile from './pages/Profile';
import BookManagement from './pages/BookManagement';
import LoanManagement from './pages/LoanManagement';
import BookCopyManagement from './pages/BookCopyManagement';
import FineManagement from './pages/FineManagement';
import ReportManagement from './pages/ReportManagement';
import UserManagement from './pages/UserManagement';
import Register from './pages/Register';
import Categories from './pages/Categories';
import About from './pages/About';

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
  // ซ่อน Navbar ทั้งหน้า Login และหน้า Register เพื่อให้ฟอร์มดูสะอาดตา
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';

  return (
    <div className="app-root min-h-screen">
      {!isAuthPage && <Navbar />}

      <Routes>
        <Route path="/login" element={<Login />} />
        {/* เพิ่ม Route สำหรับหน้าสมัครสมาชิกที่นี่ */}
        <Route path="/register" element={<Register />} />
        <Route path="/categories" element={<Categories />} />
        <Route path="/about" element={<About />} />

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
          โครงสร้าง Route สำหรับเฟส 3:
          หน้า Admin/Librarian บังคับว่าต้องมี Role ตรงตามที่กำหนดเท่านั้นถึงจะเข้าได้
        */}
        <Route
          path="/admin/books"
          element={
            <ProtectedRoute allowedRoles={['LIBRARIAN', 'ADMIN']}>
              <BookManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/loans"
          element={
            <ProtectedRoute allowedRoles={['LIBRARIAN', 'ADMIN']}>
              <LoanManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/books/:id/copies"
          element={
            <ProtectedRoute allowedRoles={['LIBRARIAN', 'ADMIN']}>
              <BookCopyManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/fines"
          element={
            <ProtectedRoute allowedRoles={['LIBRARIAN', 'ADMIN']}>
              <FineManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/reports"
          element={
            <ProtectedRoute allowedRoles={['LIBRARIAN', 'ADMIN']}>
              <ReportManagement />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/users"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <UserManagement />
            </ProtectedRoute>
          }
        />
      </Routes>
    </div>
  );
}

export default function App() {
  const [language, setLanguage] = useState('th');

  return (
    <LanguageContext.Provider value={{ language, setLanguage }}>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </LanguageContext.Provider>
  );
}
