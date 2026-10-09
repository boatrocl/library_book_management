import { useContext, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContextValue';
import { LanguageContext } from '../context/LanguageContext';

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
  const location = useLocation();
  const { user, logout } = useContext(AuthContext);
  const { language, setLanguage } = useContext(LanguageContext);
  const [menuOpen, setMenuOpen] = useState(false);
  const canManage = user && ['LIBRARIAN', 'ADMIN'].includes(user.role);
  const isCatalog = location.pathname === '/';

  function handleLogout() {
    logout();
    setMenuOpen(false);
    navigate('/login');
  }

  function closeMenu() {
    setMenuOpen(false);
  }

  return (
    <header className="site-header">
      <div className="site-header__inner">
        <Link to="/" className="brand" aria-label="LibraFlow หน้าหลัก" onClick={closeMenu}>
          <BrandMark />
          <span className="brand__wordmark">LibraFlow</span>
        </Link>

        <button
          className={`mobile-menu-button${menuOpen ? ' is-open' : ''}`}
          type="button"
          aria-label={menuOpen ? 'ปิดเมนู' : 'เปิดเมนู'}
          aria-expanded={menuOpen}
          aria-controls="primary-navigation"
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span /><span /><span />
        </button>

        <div className={`site-header__content${menuOpen ? ' is-open' : ''}`} id="primary-navigation">
          <nav className="site-nav" aria-label="เมนูหลัก">
            <NavLink to="/" end className={linkClass} onClick={closeMenu}>{language === 'th' ? 'แคตตาล็อก' : 'Catalog'}</NavLink>
            <NavLink to="/categories" className={linkClass} onClick={closeMenu}>{language === 'th' ? 'หมวดหมู่' : 'Categories'}</NavLink>
            <NavLink to="/about" className={linkClass} onClick={closeMenu}>{language === 'th' ? 'เกี่ยวกับเรา' : 'About'}</NavLink>
            {user && <NavLink to="/profile" className={linkClass} onClick={closeMenu}>โปรไฟล์ส่วนตัว</NavLink>}
            {canManage && (
              <details className="management-menu">
                <summary>จัดการระบบ <span aria-hidden="true">⌄</span></summary>
                <div className="management-menu__panel">
                  <NavLink to="/admin/books" className={linkClass} onClick={closeMenu}>จัดการหนังสือ</NavLink>
                  <NavLink to="/admin/loans" className={linkClass} onClick={closeMenu}>จัดการใบยืม</NavLink>
                  <NavLink to="/admin/fines" className={linkClass} onClick={closeMenu}>จัดการค่าปรับ</NavLink>
                  <NavLink to="/admin/reports" className={linkClass} onClick={closeMenu}>ออกรายงาน</NavLink>
                  {user.role === 'ADMIN' && <NavLink to="/admin/users" className={linkClass} onClick={closeMenu}>จัดการผู้ใช้งาน</NavLink>}
                </div>
              </details>
            )}
          </nav>

          <div className="site-header__account">
            {isCatalog && (
              <div className="language-toggle" role="group" aria-label="Select language">
                <button
                  className={`language-toggle__option${language === 'th' ? ' is-active' : ''}`}
                  type="button"
                  aria-pressed={language === 'th'}
                  onClick={() => setLanguage('th')}
                >TH</button>
                <button
                  className={`language-toggle__option${language === 'en' ? ' is-active' : ''}`}
                  type="button"
                  aria-pressed={language === 'en'}
                  onClick={() => setLanguage('en')}
                >EN</button>
              </div>
            )}
            {user ? (
              <>
                <span className="account-greeting"><span className="account-greeting__dot" />สวัสดี, {user.username}</span>
                <button className="button button--outline site-header__logout" type="button" onClick={handleLogout}>ออกจากระบบ</button>
              </>
            ) : (
              <Link className="button button--primary site-header__login" to="/login" onClick={closeMenu}>
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="8" r="3.2" stroke="currentColor" strokeWidth="1.7" /><path d="M5.5 20c.6-3.7 2.8-5.6 6.5-5.6s5.9 1.9 6.5 5.6" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" /></svg>
                {language === 'th' ? 'เข้าสู่ระบบ' : 'Sign in'}
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
