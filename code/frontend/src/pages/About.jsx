import { Link } from 'react-router-dom';

const FEATURES = [
  { icon: '▤', title: 'ค้นหาได้ง่าย', description: 'เลือกดูหนังสือจากชื่อ ผู้แต่ง และหมวดหมู่ที่สนใจ' },
  { icon: '↗', title: 'ติดตามการยืม', description: 'ดูรายการยืมและสถานะหนังสือได้จากพื้นที่สมาชิก' },
  { icon: '◷', title: 'จัดการห้องสมุด', description: 'เครื่องมือสำหรับบรรณารักษ์และผู้ดูแลระบบในที่เดียว' },
];

export default function About() {
  return (
    <main className="lf-public-page lf-about-page">
      <section className="lf-about-hero" aria-labelledby="about-heading">
        <div className="lf-about-hero__copy">
          <p className="lf-eyebrow">พื้นที่สำหรับคนรักการอ่าน</p>
          <h1 id="about-heading">เกี่ยวกับ <span>LibraFlow</span></h1>
          <p>LibraFlow คือระบบจัดการห้องสมุดที่ช่วยให้การค้นหาหนังสือ การยืม-คืน และการดูแลรายการหนังสือเป็นเรื่องที่เข้าถึงง่าย</p>
          <div className="lf-about-hero__actions">
            <Link className="button button--primary" to="/">เริ่มค้นหาหนังสือ</Link>
            <Link className="button button--secondary" to="/categories">เลือกหมวดหมู่</Link>
          </div>
        </div>
        <div className="lf-about-hero__image" aria-hidden="true">
          <img src="/libraflow-hero.svg" alt="" />
          <span className="lf-about-hero__quote">Good Books<br />Brighter People</span>
        </div>
      </section>

      <section className="lf-about-features" aria-labelledby="about-features-heading">
        <div className="lf-section-heading">
          <div>
            <p className="lf-eyebrow">ทุกเรื่องราวในที่เดียว</p>
            <h2 id="about-features-heading">ออกแบบมาเพื่อการอ่านที่ราบรื่น</h2>
          </div>
        </div>
        <div className="lf-feature-grid">
          {FEATURES.map((feature) => (
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
