import { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContextValue';
import AuthLayout from '../components/AuthLayout';
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
    <AuthLayout
      title="สร้างบัญชีผู้ใช้"
      eyebrow="สมัครสมาชิก"
      description="เริ่มต้นการเดินทางในโลกหนังสือกับ LibraFlow"
      variant="register"
      footer={<>มีบัญชีอยู่แล้วใช่หรือไม่? <Link to="/login">เข้าสู่ระบบ</Link></>}
    >
      {errorMsg && <div className="lf-form-message lf-form-message--error" role="alert">{errorMsg}</div>}
      {successMsg && <div className="lf-form-message lf-form-message--success" role="status">{successMsg}</div>}

      <form onSubmit={handleRegister} className="lf-auth-form lf-auth-form--register">
        <div className="lf-field">
          <label htmlFor="register-username">ชื่อผู้ใช้</label>
          <input id="register-username" type="text" name="username" placeholder="ตั้งชื่อผู้ใช้งาน" autoComplete="username" value={formData.username} onChange={handleChange} required />
        </div>
        <div className="lf-field">
          <label htmlFor="register-password">รหัสผ่าน</label>
          <input id="register-password" type="password" name="password" placeholder="รหัสผ่านอย่างน้อย 6 ตัวอักษร" autoComplete="new-password" value={formData.password} onChange={handleChange} required />
        </div>
        <div className="lf-field">
          <label htmlFor="register-email">อีเมล</label>
          <input id="register-email" type="email" name="email" placeholder="your@email.com" autoComplete="email" value={formData.email} onChange={handleChange} required />
        </div>
        <div className="lf-field-grid">
          <div className="lf-field">
            <label htmlFor="register-first-name">ชื่อจริง</label>
            <input id="register-first-name" type="text" name="firstName" autoComplete="given-name" value={formData.firstName} onChange={handleChange} required />
          </div>
          <div className="lf-field">
            <label htmlFor="register-last-name">นามสกุล</label>
            <input id="register-last-name" type="text" name="lastName" autoComplete="family-name" value={formData.lastName} onChange={handleChange} required />
          </div>
        </div>
        <div className="lf-field">
          <label htmlFor="register-phone">เบอร์โทรศัพท์ <span>(ไม่บังคับ)</span></label>
          <input id="register-phone" type="tel" name="phoneNumber" placeholder="08X-XXX-XXXX" autoComplete="tel" value={formData.phoneNumber} onChange={handleChange} />
        </div>
        <div className="lf-field">
          <label htmlFor="register-address">ที่อยู่ <span>(ไม่บังคับ)</span></label>
          <textarea id="register-address" name="address" placeholder="ที่อยู่ปัจจุบัน" autoComplete="street-address" value={formData.address} onChange={handleChange} rows="2" />
        </div>
        <button type="submit" disabled={isLoading} className="lf-form-submit">
          {isLoading ? 'กำลังประมวลผล...' : 'สร้างบัญชีผู้ใช้'}
        </button>
      </form>
    </AuthLayout>
  );
}
