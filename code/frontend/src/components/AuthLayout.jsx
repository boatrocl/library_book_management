import { Link } from 'react-router-dom';
import { useLanguage } from '../context/LanguageContext';
import LanguageToggle from './LanguageToggle';

export default function AuthLayout({ title, eyebrow, description, children, footer, variant = 'login' }) {
  const { t } = useLanguage();

  return (
    <main className={`lf-auth-page lf-auth-page--${variant}`}>
      <div className="lf-auth-layout">
        <section className="lf-auth-story" aria-label="LibraFlow">
          <Link className="lf-auth-brand" to="/" aria-label={t('LibraFlow หน้าหลัก', 'LibraFlow home')}>
            <img src="/libraflow-mark.svg" alt="" />
            <span>LibraFlow</span>
          </Link>

          <div className="lf-auth-story__copy">
            <p className="lf-eyebrow">{t('อ่าน ค้นพบ และแบ่งปัน', 'Read, discover, and share')}</p>
            <h1>{t('เรื่องราวดี ๆ เริ่มต้นที่นี่', 'A good story starts here')}</h1>
            <p>{t('เปิดประตูสู่โลกหนังสือ ค้นพบเล่มที่ใช่ และกลับมาอ่านต่อได้ทุกเมื่อ', 'Step into a world of books, find the right one, and pick up where you left off.')}</p>
          </div>

          <div className="lf-auth-story__art" aria-hidden="true">
            <img src="/libraflow-hero.svg" alt="" />
            <span className="lf-auth-story__quote">{t('หนังสือดี ๆ', 'A good book')}<br />{t('เปิดโลกใบใหม่', 'opens a new world.')}</span>
          </div>

          <p className="lf-auth-story__note">{t('ห้องสมุดดิจิทัล LibraFlow · พื้นที่ของคนรักการอ่าน', 'LibraFlow digital library · A place for readers')}</p>
        </section>

        <section className={`lf-auth-card lf-auth-card--${variant}`} aria-labelledby="auth-heading">
          <div className="lf-auth-card__locale"><LanguageToggle /></div>
          <div className="lf-auth-card__heading">
            <span className="lf-eyebrow">{eyebrow}</span>
            <h2 id="auth-heading">{title}</h2>
            <p>{description}</p>
          </div>
          {children}
          {footer && <div className="lf-auth-card__footer">{footer}</div>}
        </section>
      </div>
    </main>
  );
}
