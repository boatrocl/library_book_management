import { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContextValue';
import AuthLayout from '../components/AuthLayout';
import api from '../api';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const navigate = useNavigate();

  // ดึงฟังก์ชัน login จาก Context เพื่อกระจายสถานะไปทั้งแอปพลิเคชัน
  const { login } = useContext(AuthContext);

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    try {
      const response = await api.post('/api/v1/auth/login', { username, password });

      // เรียกใช้ฟังก์ชัน login จาก Context (ระบบจะบันทึก Token และอัปเดตสถานะทันที)
      login(response.data.token);

      navigate('/');
    } catch (error) {
      // ดึง Error Message มาตรฐานของ Backend มาแสดงผล ถ้ามี
      if (error.response && error.response.data && error.response.data.message) {
        setErrorMsg(error.response.data.message);
      } else if (error.response && error.response.status === 401) {
        setErrorMsg('ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง');
      } else {
        setErrorMsg('เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์');
      }
    }
  };

  return (
    <AuthLayout
      title="ยินดีต้อนรับกลับ"
      eyebrow="เข้าสู่ระบบ"
      description="เข้าสู่บัญชีเพื่อเลือกอ่านและจัดการรายการของคุณ"
      footer={<>ยังไม่มีบัญชีใช่หรือไม่? <Link to="/register">สมัครสมาชิก</Link></>}
    >
      {errorMsg && <div className="lf-form-message lf-form-message--error" role="alert">{errorMsg}</div>}

      <form onSubmit={handleLogin} className="lf-auth-form">
        <div className="lf-field">
          <label htmlFor="login-username">ชื่อผู้ใช้</label>
          <input
            id="login-username"
            type="text"
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="lf-field">
          <label htmlFor="login-password">รหัสผ่าน</label>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="lf-form-submit">เข้าสู่ระบบ</button>
      </form>
    </AuthLayout>
  );
}
