import { Link } from 'react-router-dom';

export default function AuthLayout({ title, eyebrow, description, children, footer, variant = 'login' }) {
  return (
    <main className={`lf-auth-page lf-auth-page--${variant}`}>
      <div className="lf-auth-layout">
        <section className="lf-auth-story" aria-label="LibraFlow">
          <Link className="lf-auth-brand" to="/" aria-label="LibraFlow หน้าหลัก">
            <img src="/libraflow-mark.svg" alt="" />
            <span>LibraFlow</span>
          </Link>

          <div className="lf-auth-story__copy">
            <p className="lf-eyebrow">อ่าน ค้นพบ และแบ่งปัน</p>
            <h1>เรื่องราวดี ๆ เริ่มต้นที่นี่</h1>
            <p>เปิดประตูสู่โลกหนังสือ ค้นพบเล่มที่ใช่ และกลับมาอ่านต่อได้ทุกเมื่อ</p>
          </div>

          <div className="lf-auth-story__art" aria-hidden="true">
            <img src="/libraflow-hero.svg" alt="" />
            <span className="lf-auth-story__quote">A good book<br />opens a new world.</span>
          </div>

          <p className="lf-auth-story__note">ห้องสมุดดิจิทัล LibraFlow · พื้นที่ของคนรักการอ่าน</p>
        </section>

        <section className={`lf-auth-card lf-auth-card--${variant}`} aria-labelledby="auth-heading">
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
