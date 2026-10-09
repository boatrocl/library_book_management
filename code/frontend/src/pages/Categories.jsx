import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';

const ART_VARIANTS = ['books', 'globe', 'light', 'leaf', 'screen', 'heart', 'briefcase', 'mountain'];

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    async function loadCategories() {
      setIsLoading(true);
      setHasError(false);
      try {
        const response = await api.get('/api/v1/categories', { signal: controller.signal });
        if (!controller.signal.aborted) setCategories(response.data || []);
      } catch {
        if (!controller.signal.aborted) setHasError(true);
      } finally {
        if (!controller.signal.aborted) setIsLoading(false);
      }
    }

    void loadCategories();
    return () => controller.abort();
  }, [retry]);

  return (
    <main className="lf-public-page lf-categories-page">
      <section className="lf-public-hero" aria-labelledby="categories-heading">
        <div className="lf-public-hero__copy">
          <p className="lf-eyebrow">เลือกเรื่องราวที่สนใจ</p>
          <h1 id="categories-heading">หมวดหมู่หนังสือ</h1>
          <p>สำรวจหนังสือหลากหลายแนว แล้วเริ่มต้นอ่านเรื่องถัดไปที่เหมาะกับคุณ</p>
          <Link className="button button--primary" to="/#books">ดูหนังสือทั้งหมด <span aria-hidden="true">→</span></Link>
        </div>
        <div className="lf-public-hero__art" aria-hidden="true">
          <img src="/libraflow-hero.svg" alt="" />
        </div>
      </section>

      <section className="lf-page-section" aria-labelledby="category-grid-heading">
        <div className="lf-section-heading">
          <div>
            <p className="lf-eyebrow">ชั้นหนังสือของเรา</p>
            <h2 id="category-grid-heading">เลือกหมวดหมู่ที่ใช่</h2>
          </div>
          {!isLoading && !hasError && <span className="lf-count-pill">{categories.length} หมวดหมู่</span>}
        </div>

        {isLoading ? (
          <div className="lf-category-grid" aria-label="กำลังโหลดหมวดหมู่" aria-busy="true">
            {Array.from({ length: 6 }, (_, index) => <div className="lf-category-skeleton" key={index} />)}
          </div>
        ) : hasError ? (
          <div className="lf-empty-state" role="alert">
            <span className="lf-empty-state__icon" aria-hidden="true">!</span>
            <h3>โหลดหมวดหมู่ไม่สำเร็จ</h3>
            <p>ตรวจสอบการเชื่อมต่อแล้วลองอีกครั้ง</p>
            <button className="button button--primary" type="button" onClick={() => setRetry((value) => value + 1)}>ลองอีกครั้ง</button>
          </div>
        ) : categories.length === 0 ? (
          <div className="lf-empty-state">
            <span className="lf-empty-state__icon" aria-hidden="true">▤</span>
            <h3>ยังไม่มีหมวดหมู่ในระบบ</h3>
            <p>กลับมาดูอีกครั้งเมื่อมีการเพิ่มหมวดหมู่หนังสือ</p>
          </div>
        ) : (
          <div className="lf-category-grid">
            {categories.map((category, index) => (
              <Link
                className="lf-category-card"
                key={category.id}
                to={`/?categoryId=${encodeURIComponent(category.id)}#categories`}
              >
                <span className={`lf-category-card__art lf-category-card__art--${ART_VARIANTS[index % ART_VARIANTS.length]}`} aria-hidden="true">
                  <span className="lf-category-card__book lf-category-card__book--one" />
                  <span className="lf-category-card__book lf-category-card__book--two" />
                  <span className="lf-category-card__book lf-category-card__book--three" />
                  <span className="lf-category-card__spark">✦</span>
                </span>
                <span className="lf-category-card__name">{category.name}</span>
                <span className="lf-category-card__action">เลือกหมวดหมู่นี้ <span aria-hidden="true">›</span></span>
              </Link>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
