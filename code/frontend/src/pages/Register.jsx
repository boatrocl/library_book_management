import { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../api';

export default function Register() {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    address: ''
  });

  const { login } = useContext(AuthContext);

  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');
    setIsLoading(true);
    
    try {
      const response = await api.post('/api/v1/auth/register', formData);
      setSuccessMsg('สมัครสมาชิกสำเร็จ! กำลังเข้าสู่ระบบ...');

      login(response.data.token);

      setTimeout(() => {
        navigate('/');
      }, 2000);
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        setErrorMsg(error.response.data.message);
      } else {
        setErrorMsg('เกิดข้อผิดพลาดในการสมัครสมาชิก กรุณาลองใหม่อีกครั้ง');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100 py-10 px-4">
      {/* ปรับขนาดกล่องให้แคบลงเป็น max-w-md เท่าหน้า Login */}
      <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-xl shadow-md">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-800">สมัครสมาชิก</h2>
          <p className="text-sm text-gray-500 mt-1">เข้าร่วมเป็นสมาชิก LibraFlow</p>
        </div>
        
        {errorMsg && (
          <div className="p-3 text-sm text-red-700 bg-red-100 rounded-md text-center">
            {errorMsg}
          </div>
        )}
        {successMsg && (
          <div className="p-3 text-sm text-green-700 bg-green-100 rounded-md text-center">
            {successMsg}
          </div>
        )}

        {/* บีบทุกฟิลด์ให้เป็นแนวตั้ง (space-y-4) */}
        <form onSubmit={handleRegister} className="space-y-4">
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">ชื่อผู้ใช้</label>
            <input 
              type="text" 
              name="username" 
              placeholder="ตั้งชื่อผู้ใช้งาน"
              value={formData.username} 
              onChange={handleChange} 
              className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
              required 
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">รหัสผ่าน</label>
            <input 
              type="password" 
              name="password" 
              placeholder="รหัสผ่านอย่างน้อย 6 ตัวอักษร"
              value={formData.password} 
              onChange={handleChange} 
              className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
              required 
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">อีเมล</label>
            <input 
              type="email" 
              name="email" 
              placeholder="your@email.com"
              value={formData.email} 
              onChange={handleChange} 
              className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
              required 
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">ชื่อจริง</label>
            <input 
              type="text" 
              name="firstName" 
              placeholder="ชื่อจริง"
              value={formData.firstName} 
              onChange={handleChange} 
              className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
              required 
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">นามสกุล</label>
            <input 
              type="text" 
              name="lastName" 
              placeholder="นามสกุล"
              value={formData.lastName} 
              onChange={handleChange} 
              className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
              required 
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">เบอร์โทรศัพท์</label>
            <input 
              type="text" 
              name="phoneNumber" 
              placeholder="08X-XXX-XXXX (ไม่บังคับ)"
              value={formData.phoneNumber} 
              onChange={handleChange} 
              className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" 
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">ที่อยู่</label>
            <textarea 
              name="address" 
              placeholder="ที่อยู่ปัจจุบัน (ไม่บังคับ)"
              value={formData.address} 
              onChange={handleChange} 
              className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none" 
              rows="2"
            ></textarea>
          </div>

          <button 
            type="submit" 
            disabled={isLoading}
            className={`w-full py-2 mt-2 font-bold text-white rounded-md transition ${isLoading ? 'bg-blue-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'}`}
          >
            {isLoading ? 'กำลังประมวลผล...' : 'สมัครสมาชิก'}
          </button>
        </form>

        <div className="text-sm text-center text-gray-600">
          มีบัญชีอยู่แล้วใช่หรือไม่?{' '}
          <button 
            type="button"
            onClick={() => navigate('/login')} 
            className="text-blue-600 hover:underline font-medium"
          >
            เข้าสู่ระบบ
          </button>
        </div>
      </div>
    </div>
  );
}