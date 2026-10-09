import { Link, useNavigate } from 'react-router-dom';
import { useContext } from 'react';
import { AuthContext } from '../context/AuthContextValue';

export default function Navbar() {
  const navigate = useNavigate();
  // ดึงข้อมูลผู้ใช้และฟังก์ชัน logout จาก Context
  const { user, logout } = useContext(AuthContext);

  const handleLogout = () => {
    logout(); 
    navigate('/login');
  };

  return (
    <nav className="bg-blue-700 shadow-md">
      <div className="max-w-6xl px-4 mx-auto">
        <div className="flex items-center justify-between h-16">
          
          {/*เมนูซ้าย*/}
          <div className="flex items-center space-x-8">
            <Link to="/" className="text-2xl font-bold text-white tracking-wider">
              LibraFlow
            </Link>
            <div className="flex space-x-4">
              <Link to="/" className="px-3 py-2 text-sm font-medium text-white rounded-md hover:bg-blue-600 transition">
                แคตตาล็อก
              </Link>
              
              {/* แสดงปุ่มนี้เฉพาะเมื่อล็อกอินแล้วเท่านั้น */}
              {user && (
                <Link to="/profile" className="px-3 py-2 text-sm font-medium text-white rounded-md hover:bg-blue-600 transition">
                  โปรไฟล์ส่วนตัว
                </Link>
              )}

              {/* แสดงเมนูจัดการระบบเฉพาะ Role LIBRARIAN หรือ ADMIN เท่านั้น */}
              {user && (user.role === 'LIBRARIAN' || user.role === 'ADMIN') && (
                <>
                  <Link to="/admin/books" className="px-3 py-2 text-sm font-medium text-yellow-300 rounded-md hover:bg-blue-600 transition">
                    จัดการหนังสือ
                  </Link>
                  <Link to="/admin/loans" className="px-3 py-2 text-sm font-medium text-yellow-300 rounded-md hover:bg-blue-600 transition">
                    จัดการใบยืม
                  </Link>
                  <Link to="/admin/fines" className="px-3 py-2 text-sm font-medium text-yellow-300 rounded-md hover:bg-blue-600 transition">
                    จัดการค่าปรับ
                  </Link>
                  <Link to="/admin/reports" className="px-3 py-2 text-sm font-medium text-yellow-300 rounded-md hover:bg-blue-600 transition">
                    ออกรายงาน
                  </Link>
                </>
              )}

              {user?.role === 'ADMIN' && (
                <Link to="/admin/users" className="px-3 py-2 text-sm font-medium text-yellow-300 rounded-md hover:bg-blue-600 transition">
                  จัดการผู้ใช้งาน
                </Link>
              )}
            </div>
          </div>

          {/*ปุ่มขวา*/}
          <div className="flex items-center space-x-4">
            {user ? (
              <>
                <span className="text-sm font-medium text-blue-200">
                  สวัสดี, {user.username}
                </span>
                <button 
                  onClick={handleLogout}
                  className="px-4 py-2 text-sm font-bold text-blue-700 bg-white rounded-md hover:bg-gray-100 transition shadow-xs"
                >
                  ออกจากระบบ
                </button>
              </>
            ) : (
              <Link 
                to="/login"
                className="px-4 py-2 text-sm font-bold text-blue-700 bg-white rounded-md hover:bg-gray-100 transition shadow-xs"
              >
                เข้าสู่ระบบ
              </Link>
            )}
          </div>

        </div>
      </div>
    </nav>
  );
}
