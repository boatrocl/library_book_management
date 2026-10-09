import { useContext, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContextValue';
import { useLanguage } from '../context/LanguageContext';
import LanguageToggle from './LanguageToggle';

function BrandMark() {
  return (
    <svg className="brand-mark" viewBox="0 0 72 48" fill="none" aria-hidden="true">
      <path d="M4 10c12 0 22 4 32 15v17C25 29 15 25 4 25V10Z" fill="#54A9BD" />
      <path d="M68 5C56 5 46 9 36 20v22c11-13 21-17 32-17V5Z" fill="#102C54" />
      <path d="M10 5c10 1 18 5 26 14v6C26 16 18 12 10 11V5Z" fill="#8BC8D2" />
      <path d="M62 1C52 2 44 6 36 14v6C46 10 54 7 62 7V1Z" fill="#08798A" />
      <path d="M8 31c10 1 18 5 27 13m29-17c-10 1-18 5-27 13" stroke="#D9EBE7" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

function linkClass({ isActive }) {
  return `site-nav__link${isActive ? ' is-active' : ''}`;
}

export default function Navbar() {
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);
  const { t } = useLanguage();
  const [menuOpen, setMenuOpen] = useState(false);
  const managementMenuRef = useRef(null);
  const canManage = user && ['LIBRARIAN', 'ADMIN'].includes(user.role);

  function handleLogout() {
    logout();
    setMenuOpen(false);
    navigate('/login');
  }

  function closeMenu() {
    setMenuOpen(false);
    if (managementMenuRef.current) {
      managementMenuRef.current.open = false;
    }
  }

  return (
    <header className="site-header">
      <div className="site-header__inner">
        <Link to="/" className="brand" aria-label={t('LibraFlow หน้าหลัก', 'LibraFlow home')} onClick={closeMenu}>
          <BrandMark />
          <span className="brand__wordmark">LibraFlow</span>
        </Link>

        <button
          className={`mobile-menu-button${menuOpen ? ' is-open' : ''}`}
          type="button"
          aria-label={menuOpen ? t('ปิดเมนู', 'Close menu') : t('เปิดเมนู', 'Open menu')}
          aria-expanded={menuOpen}
          aria-controls="primary-navigation"
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span /><span /><span />
        </button>

        <div className={`site-header__content${menuOpen ? ' is-open' : ''}`} id="primary-navigation">
          <nav className="site-nav" aria-label={t('เมนูหลัก', 'Main navigation')}>
            <NavLink to="/" end className={linkClass} onClick={closeMenu}>{t('แคตตาล็อก', 'Catalog')}</NavLink>
            <NavLink to="/categories" className={linkClass} onClick={closeMenu}>{t('หมวดหมู่', 'Categories')}</NavLink>
            <NavLink to="/rules" className={linkClass} onClick={closeMenu}>{t('กฎและเงื่อนไข', 'Library rules')}</NavLink>
            <NavLink to="/about" className={linkClass} onClick={closeMenu}>{t('เกี่ยวกับเรา', 'About')}</NavLink>
            {user && <NavLink to="/profile" className={linkClass} onClick={closeMenu}>{t('โปรไฟล์ส่วนตัว', 'My profile')}</NavLink>}
            {canManage && (
              <details className="management-menu" ref={managementMenuRef}>
                <summary>{t('จัดการระบบ', 'Management')} <span aria-hidden="true">⌄</span></summary>
                <div className="management-menu__panel">
                  <NavLink to="/admin/books" className={linkClass} onClick={closeMenu}>{t('จัดการหนังสือ', 'Manage books')}</NavLink>
                  <NavLink to="/admin/loans" className={linkClass} onClick={closeMenu}>{t('จัดการการยืม-คืน', 'Manage loans and returns')}</NavLink>
                  <NavLink to="/admin/fines" className={linkClass} onClick={closeMenu}>{t('จัดการค่าปรับ', 'Manage fines')}</NavLink>
                  <NavLink to="/admin/reports" className={linkClass} onClick={closeMenu}>{t('ออกรายงาน', 'Reports')}</NavLink>
                  {user.role === 'ADMIN' && <NavLink to="/admin/users" className={linkClass} onClick={closeMenu}>{t('จัดการผู้ใช้งาน', 'Manage users')}</NavLink>}
                </div>
              </details>
            )}
          </nav>

          <div className="site-header__account">
            <LanguageToggle />
            {user ? (
              <>
                <span className="account-greeting"><span className="account-greeting__dot" />{t(`สวัสดี, ${user.username}`, `Hello, ${user.username}`)}</span>
                <button className="button button--outline site-header__logout" type="button" onClick={handleLogout}>{t('ออกจากระบบ', 'Sign out')}</button>
              </>
            ) : (
              <Link className="button button--primary site-header__login" to="/login" onClick={closeMenu}>
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="8" r="3.2" stroke="currentColor" strokeWidth="1.7" /><path d="M5.5 20c.6-3.7 2.8-5.6 6.5-5.6s5.9 1.9 6.5 5.6" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" /></svg>
                {t('เข้าสู่ระบบ', 'Sign in')}
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
