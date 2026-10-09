import { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContextValue';
import AuthLayout from '../components/AuthLayout';
import { useLanguage } from '../context/LanguageContext';
import api from '../api';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState({ th: '', en: '' });
  const navigate = useNavigate();
  const { t } = useLanguage();

  // ดึงฟังก์ชัน login จาก Context เพื่อกระจายสถานะไปทั้งแอปพลิเคชัน
  const { login } = useContext(AuthContext);

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMsg({ th: '', en: '' });

    try {
      const response = await api.post('/api/v1/auth/login', { username, password });

      // เรียกใช้ฟังก์ชัน login จาก Context (ระบบจะบันทึก Token และอัปเดตสถานะทันที)
      login(response.data.token);

      navigate('/');
    } catch (error) {
      // ดึง Error Message มาตรฐานของ Backend มาแสดงผล ถ้ามี
      const backendMessage = error.response?.data?.message;
      const invalidCredentials = error.response?.status === 401;
      setErrorMsg({
        th: backendMessage || (invalidCredentials ? 'ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง' : 'เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์'),
        en: backendMessage ? 'Sign-in failed. Check your details and try again.' : (invalidCredentials ? 'The username or password is incorrect.' : 'Could not connect to the server.'),
      });
    }
  };

  return (
    <AuthLayout
      title={t('ยินดีต้อนรับกลับ', 'Welcome back')}
      eyebrow={t('เข้าสู่ระบบ', 'Sign in')}
      description={t('เข้าสู่บัญชีเพื่อเลือกอ่านและจัดการรายการของคุณ', 'Sign in to browse books and manage your library activity.')}
      footer={<>{t('ยังไม่มีบัญชีใช่หรือไม่?', "Don't have an account?")} <Link to="/register">{t('สมัครสมาชิก', 'Create an account')}</Link></>}
    >
      {(errorMsg.th || errorMsg.en) && <div className="lf-form-message lf-form-message--error" role="alert">{t(errorMsg.th, errorMsg.en)}</div>}

      <form onSubmit={handleLogin} className="lf-auth-form">
        <div className="lf-field">
          <label htmlFor="login-username">{t('ชื่อผู้ใช้', 'Username')}</label>
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
          <label htmlFor="login-password">{t('รหัสผ่าน', 'Password')}</label>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="lf-form-submit">{t('เข้าสู่ระบบ', 'Sign in')}</button>
      </form>
    </AuthLayout>
  );
}
