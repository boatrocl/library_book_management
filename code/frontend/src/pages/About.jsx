import { Link } from 'react-router-dom';
import { useLanguage } from '../context/LanguageContext';

export default function About() {
  const { t } = useLanguage();
  const features = [
    { icon: '▤', title: t('ค้นหาได้ง่าย', 'Easy discovery'), description: t('เลือกดูหนังสือจากชื่อ ผู้แต่ง และหมวดหมู่ที่สนใจ', 'Browse books by title, author, and the categories you enjoy.') },
    { icon: '↗', title: t('ติดตามการยืม', 'Track your loans'), description: t('ดูรายการยืมและสถานะหนังสือได้จากพื้นที่สมาชิก', 'View your loans and book statuses from your member profile.') },
    { icon: '◷', title: t('จัดการห้องสมุด', 'Library management'), description: t('เครื่องมือสำหรับบรรณารักษ์และผู้ดูแลระบบในที่เดียว', 'Tools for librarians and administrators in one place.') },
  ];

  return (
    <main className="lf-public-page lf-about-page">
      <section className="lf-about-hero" aria-labelledby="about-heading">
        <div className="lf-about-hero__copy">
          <p className="lf-eyebrow">{t('พื้นที่สำหรับคนรักการอ่าน', 'A space for readers')}</p>
          <h1 id="about-heading">{t('เกี่ยวกับ', 'About')} <span>LibraFlow</span></h1>
          <p>{t('LibraFlow คือระบบจัดการห้องสมุดที่ช่วยให้การค้นหาหนังสือ การยืม-คืน และการดูแลรายการหนังสือเป็นเรื่องที่เข้าถึงง่าย', 'LibraFlow makes it easy to discover books, manage loans and returns, and keep library collections organized.')}</p>
          <div className="lf-about-hero__actions">
            <Link className="button button--primary" to="/">{t('เริ่มค้นหาหนังสือ', 'Explore the catalog')}</Link>
            <Link className="button button--secondary" to="/categories">{t('เลือกหมวดหมู่', 'Browse categories')}</Link>
          </div>
        </div>
        <div className="lf-about-hero__image" aria-hidden="true">
          <img src="/libraflow-hero.svg" alt="" />
          <span className="lf-about-hero__quote">{t('หนังสือดี ๆ', 'Good Books')}<br />{t('สร้างแรงบันดาลใจ', 'Brighter People')}</span>
        </div>
      </section>

      <section className="lf-about-features" aria-labelledby="about-features-heading">
        <div className="lf-section-heading">
          <div>
            <p className="lf-eyebrow">{t('ทุกเรื่องราวในที่เดียว', 'Everything in one place')}</p>
            <h2 id="about-features-heading">{t('ออกแบบมาเพื่อการอ่านที่ราบรื่น', 'Designed for a smoother reading journey')}</h2>
          </div>
        </div>
        <div className="lf-feature-grid">
          {features.map((feature) => (
            <article className="lf-feature-card" key={feature.title}>
              <span className="lf-feature-card__icon" aria-hidden="true">{feature.icon}</span>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
