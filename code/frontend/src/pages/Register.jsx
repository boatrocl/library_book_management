import { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContextValue';
import AuthLayout from '../components/AuthLayout';
import { useLanguage } from '../context/LanguageContext';
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
  const { t } = useLanguage();

  const [errorMsg, setErrorMsg] = useState({ th: '', en: '' });
  const [successMsg, setSuccessMsg] = useState({ th: '', en: '' });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setErrorMsg({ th: '', en: '' });
    setSuccessMsg({ th: '', en: '' });
    setIsLoading(true);

    try {
      const response = await api.post('/api/v1/auth/register', formData);
      setSuccessMsg({ th: 'สมัครสมาชิกสำเร็จ! กำลังเข้าสู่ระบบ...', en: 'Account created. Signing you in...' });

      login(response.data.token);

      setTimeout(() => {
        navigate('/');
      }, 2000);
    } catch (error) {
      const backendMessage = error.response?.data?.message;
      setErrorMsg({
        th: backendMessage || 'เกิดข้อผิดพลาดในการสมัครสมาชิก กรุณาลองใหม่อีกครั้ง',
        en: backendMessage ? 'Registration failed. Check your details and try again.' : 'Could not create your account. Please try again.',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title={t('สร้างบัญชีผู้ใช้', 'Create your account')}
      eyebrow={t('สมัครสมาชิก', 'Register')}
      description={t('เริ่มต้นการเดินทางในโลกหนังสือกับ LibraFlow', 'Start your reading journey with LibraFlow.')}
      variant="register"
      footer={<>{t('มีบัญชีอยู่แล้วใช่หรือไม่?', 'Already have an account?')} <Link to="/login">{t('เข้าสู่ระบบ', 'Sign in')}</Link></>}
    >
      {(errorMsg.th || errorMsg.en) && <div className="lf-form-message lf-form-message--error" role="alert">{t(errorMsg.th, errorMsg.en)}</div>}
      {(successMsg.th || successMsg.en) && <div className="lf-form-message lf-form-message--success" role="status">{t(successMsg.th, successMsg.en)}</div>}

      <form onSubmit={handleRegister} className="lf-auth-form lf-auth-form--register">
        <div className="lf-field">
          <label htmlFor="register-username">{t('ชื่อผู้ใช้', 'Username')}</label>
          <input id="register-username" type="text" name="username" placeholder={t('ตั้งชื่อผู้ใช้งาน', 'Choose a username')} autoComplete="username" value={formData.username} onChange={handleChange} required />
        </div>
        <div className="lf-field">
          <label htmlFor="register-password">{t('รหัสผ่าน', 'Password')}</label>
          <input id="register-password" type="password" name="password" placeholder={t('รหัสผ่านอย่างน้อย 6 ตัวอักษร', 'At least 6 characters')} autoComplete="new-password" value={formData.password} onChange={handleChange} required />
        </div>
        <div className="lf-field">
          <label htmlFor="register-email">{t('อีเมล', 'Email')}</label>
          <input id="register-email" type="email" name="email" placeholder="your@email.com" autoComplete="email" value={formData.email} onChange={handleChange} required />
        </div>
        <div className="lf-field-grid">
          <div className="lf-field">
            <label htmlFor="register-first-name">{t('ชื่อจริง', 'First name')}</label>
            <input id="register-first-name" type="text" name="firstName" autoComplete="given-name" value={formData.firstName} onChange={handleChange} required />
          </div>
          <div className="lf-field">
            <label htmlFor="register-last-name">{t('นามสกุล', 'Last name')}</label>
            <input id="register-last-name" type="text" name="lastName" autoComplete="family-name" value={formData.lastName} onChange={handleChange} required />
          </div>
        </div>
        <div className="lf-field">
          <label htmlFor="register-phone">{t('เบอร์โทรศัพท์', 'Phone number')} <span>{t('(ไม่บังคับ)', '(optional)')}</span></label>
          <input id="register-phone" type="tel" name="phoneNumber" placeholder="08X-XXX-XXXX" autoComplete="tel" value={formData.phoneNumber} onChange={handleChange} />
        </div>
        <div className="lf-field">
          <label htmlFor="register-address">{t('ที่อยู่', 'Address')} <span>{t('(ไม่บังคับ)', '(optional)')}</span></label>
          <textarea id="register-address" name="address" placeholder={t('ที่อยู่ปัจจุบัน', 'Current address')} autoComplete="street-address" value={formData.address} onChange={handleChange} rows="2" />
        </div>
        <button type="submit" disabled={isLoading} className="lf-form-submit">
          {isLoading ? t('กำลังประมวลผล...', 'Creating account...') : t('สร้างบัญชีผู้ใช้', 'Create account')}
        </button>
      </form>
    </AuthLayout>
  );
}
