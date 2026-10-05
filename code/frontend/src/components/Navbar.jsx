import { Link, useNavigate } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    //ลบกุญแจออกจากกระเป๋า
    localStorage.removeItem('token');
    //กลับไปหน้า Login
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
                📚 แคตตาล็อก
              </Link>
              <Link to="/profile" className="px-3 py-2 text-sm font-medium text-white rounded-md hover:bg-blue-600 transition">
                👤 โปรไฟล์ส่วนตัว
              </Link>
            </div>
          </div>

          {/*ปุ่มขวา*/}
          <button 
            onClick={handleLogout}
            className="px-4 py-2 text-sm font-bold text-blue-700 bg-white rounded-md hover:bg-gray-100 transition shadow-sm"
          >
            ออกจากระบบ
          </button>

        </div>
      </div>
    </nav>
  );
}