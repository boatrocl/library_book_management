import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';
import { useLanguage } from '../context/LanguageContext';

const ART_VARIANTS = ['books', 'globe', 'light', 'leaf', 'screen', 'heart', 'briefcase', 'mountain'];

export default function Categories() {
  const { t, categoryName } = useLanguage();
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
          <p className="lf-eyebrow">{t('เลือกเรื่องราวที่สนใจ', 'Find your next story')}</p>
          <h1 id="categories-heading">{t('หมวดหมู่หนังสือ', 'Book categories')}</h1>
          <p>{t('สำรวจหนังสือหลากหลายแนว แล้วเริ่มต้นอ่านเรื่องถัดไปที่เหมาะกับคุณ', 'Explore a range of subjects and find a book for your next reading journey.')}</p>
          <Link className="button button--primary" to="/#books">{t('ดูหนังสือทั้งหมด', 'Browse all books')} <span aria-hidden="true">→</span></Link>
        </div>
        <div className="lf-public-hero__art" aria-hidden="true">
          <img src="/libraflow-hero.svg" alt="" />
        </div>
      </section>

      <section className="lf-page-section" aria-labelledby="category-grid-heading">
        <div className="lf-section-heading">
          <div>
            <p className="lf-eyebrow">{t('ชั้นหนังสือของเรา', 'Our shelves')}</p>
            <h2 id="category-grid-heading">{t('เลือกหมวดหมู่ที่ใช่', 'Choose a category')}</h2>
          </div>
          {!isLoading && !hasError && <span className="lf-count-pill">{t(`${categories.length.toLocaleString('th-TH')} หมวดหมู่`, `${categories.length.toLocaleString('en-US')} categories`)}</span>}
        </div>

        {isLoading ? (
          <div className="lf-category-grid" aria-label={t('กำลังโหลดหมวดหมู่', 'Loading categories')} aria-busy="true">
            {Array.from({ length: 6 }, (_, index) => <div className="lf-category-skeleton" key={index} />)}
          </div>
        ) : hasError ? (
          <div className="lf-empty-state" role="alert">
            <span className="lf-empty-state__icon" aria-hidden="true">!</span>
            <h3>{t('โหลดหมวดหมู่ไม่สำเร็จ', 'Could not load categories')}</h3>
            <p>{t('ตรวจสอบการเชื่อมต่อแล้วลองอีกครั้ง', 'Check your connection and try again.')}</p>
            <button className="button button--primary" type="button" onClick={() => setRetry((value) => value + 1)}>{t('ลองอีกครั้ง', 'Try again')}</button>
          </div>
        ) : categories.length === 0 ? (
          <div className="lf-empty-state">
            <span className="lf-empty-state__icon" aria-hidden="true">▤</span>
            <h3>{t('ยังไม่มีหมวดหมู่ในระบบ', 'No categories yet')}</h3>
            <p>{t('กลับมาดูอีกครั้งเมื่อมีการเพิ่มหมวดหมู่หนังสือ', 'Check back after book categories have been added.')}</p>
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
                <span className="lf-category-card__name">{categoryName(category.name)}</span>
                <span className="lf-category-card__action">{t('เลือกหมวดหมู่นี้', 'Browse this category')} <span aria-hidden="true">›</span></span>
              </Link>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
