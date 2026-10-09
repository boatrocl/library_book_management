import LibraryRules from '../components/LibraryRules';
import { useLanguage } from '../context/LanguageContext';

export default function Policies() {
  const { t } = useLanguage();

  return (
    <main className="library-policy-page">
      <div className="library-policy-page__inner">
        <section className="library-policy-hero">
          <span className="section-kicker">{t('ยืม อ่าน และส่งต่อ', 'Borrow, read, and share')}</span>
          <h1>{t('ใช้บริการห้องสมุดอย่างมั่นใจ', 'Enjoy the library with confidence')}</h1>
          <p>{t('ตรวจสอบวันยืม ค่าปรับ และขั้นตอนจองคิวได้ก่อนทำรายการ', 'Review loan periods, fine rates, and reservation steps before you make a request.')}</p>
        </section>
        <LibraryRules />
      </div>
    </main>
  );
}
