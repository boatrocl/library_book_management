import { useId, useState } from 'react';
import { useLanguage } from '../context/LanguageContext';

const TIERS = [
  { id: 'STUDENT', th: 'นักเรียน/นักศึกษา', en: 'Student', days: 7, quota: 5, fine: 3 },
  { id: 'STAFF', th: 'บุคลากร', en: 'Staff', days: 14, quota: 10, fine: 5, cap: 300 },
  { id: 'EXTERNAL', th: 'บุคคลภายนอก', en: 'External', days: 3, quota: 2, fine: 10 },
];

export default function LibraryRules({ compact = false }) {
  const { language, t } = useLanguage();
  const id = useId();
  const [activeTab, setActiveTab] = useState('loans');
  const tabs = [
    { id: 'loans', th: 'การยืมและคืน', en: 'Borrowing' },
    { id: 'fines', th: 'ค่าปรับ', en: 'Fines' },
    { id: 'reservations', th: 'การจองคิว', en: 'Reservations' },
  ];
  const handleTabKeyDown = (event) => {
    const currentIndex = tabs.findIndex((tab) => tab.id === activeTab);
    let nextIndex;
    if (event.key === 'ArrowRight' || event.key === 'ArrowDown') nextIndex = (currentIndex + 1) % tabs.length;
    else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') nextIndex = (currentIndex - 1 + tabs.length) % tabs.length;
    else if (event.key === 'Home') nextIndex = 0;
    else if (event.key === 'End') nextIndex = tabs.length - 1;
    else return;

    event.preventDefault();
    const nextTab = tabs[nextIndex];
    setActiveTab(nextTab.id);
    document.getElementById(`${id}-${nextTab.id}-tab`)?.focus();
  };

  return (
    <section className={`library-rules${compact ? ' library-rules--compact' : ''}`} aria-label={t('กฎและเงื่อนไขห้องสมุด', 'Library rules and conditions')}>
      {!compact && (
        <header className="library-rules__intro">
          <span className="section-kicker">{t('อ่านก่อนยืมหรือจอง', 'Before you borrow or reserve')}</span>
          <h2>{t('กฎและเงื่อนไขการใช้บริการ', 'Library rules and service terms')}</h2>
          <p>{t('รายละเอียดด้านล่างตรงกับนโยบายที่ระบบใช้คำนวณวันคืน ค่าปรับ และลำดับคิวจอง', 'These rules describe the loan periods, fine rates, and reservation queue used by the system.')}</p>
        </header>
      )}

      <div className="library-rules__tabs" role="tablist" aria-label={t('หมวดหมู่กฎห้องสมุด', 'Library rule categories')} onKeyDown={handleTabKeyDown}>
        {tabs.map((tab) => (
          <button
            key={tab.id}
            id={`${id}-${tab.id}-tab`}
            className={`library-rules__tab${activeTab === tab.id ? ' is-active' : ''}`}
            type="button"
            role="tab"
            aria-selected={activeTab === tab.id}
            aria-controls={`${id}-panel`}
            tabIndex={activeTab === tab.id ? 0 : -1}
            onClick={() => setActiveTab(tab.id)}
          >
            {language === 'en' ? tab.en : tab.th}
          </button>
        ))}
      </div>

      <div className="library-rules__panel" id={`${id}-panel`} role="tabpanel" tabIndex={0} aria-labelledby={`${id}-${activeTab}-tab`}>
        {activeTab === 'loans' && (
          <div className="library-rules__section">
            <p className="library-rules__lead">{t('ระยะเวลายืมขึ้นอยู่กับประเภทสมาชิก วันครบกำหนดจะแสดงในรายการยืมของคุณ', 'Loan periods depend on membership tier. Your due date appears in your loan history.')}</p>
            <div className="library-rules__tier-grid">
              {TIERS.map((tier) => (
                <article className="library-rules__tier" key={tier.id}>
                  <span className="library-rules__tier-code">{tier.id}</span>
                  <h3>{language === 'en' ? tier.en : tier.th}</h3>
                  <p><span>{t('ยืมได้', 'Loan period')}</span><strong>{t(`${tier.days} วัน`, `${tier.days} days`)}</strong></p>
                  <p><span>{t('โควตา', 'Loan limit')}</span><strong>{t(`${tier.quota} เล่ม`, `${tier.quota} books`)}</strong></p>
                </article>
              ))}
            </div>
            <ul className="library-rules__list">
              <li>{t('สามารถนำหนังสือมาคืนก่อนวันครบกำหนดได้ที่ห้องสมุด', 'You may return books at the library before their due date.')}</li>
              <li>{t('ต่ออายุได้ครั้งละ 7 วัน ไม่เกิน 2 ครั้ง และต่อไม่ได้เมื่อมีสมาชิกกำลังรอจองหนังสือเล่มนั้น', 'A loan can be renewed for 7 days, up to 2 times, unless another member is waiting for that title.')}</li>
            </ul>
          </div>
        )}

        {activeTab === 'fines' && (
          <div className="library-rules__section">
            <p className="library-rules__lead">{t('เมื่อเลยวันครบกำหนด ระบบนับวันที่เกินกำหนดและคำนวณค่าปรับตามประเภทสมาชิก', 'After the due date, overdue days and fines are calculated by membership tier.')}</p>
            <div className="library-rules__fine-list">
              {TIERS.map((tier) => (
                <div className="library-rules__fine-row" key={tier.id}>
                  <div><strong>{language === 'en' ? tier.en : tier.th}</strong><span>{tier.id}</span></div>
                  <b>{t(`${tier.fine} บาท/วัน`, `฿${tier.fine} / day`)}</b>
                  {tier.cap && <small>{t(`สูงสุด ${tier.cap} บาท`, `Capped at ฿${tier.cap}`)}</small>}
                </div>
              ))}
            </div>
            <p className="library-rules__note">{t('เริ่มนับวันเกินกำหนดตั้งแต่วันถัดจากวันครบกำหนด ค่าปรับจะถูกบันทึกเมื่อเจ้าหน้าที่รับคืนหนังสือ และสมาชิกที่มียอดค้างเกิน 100 บาทจะยืมเพิ่มไม่ได้', 'Overdue counting starts the day after the due date. The fine is recorded when staff process the return. Members with more than ฿100 in unpaid fines cannot borrow additional books.')}</p>
          </div>
        )}

        {activeTab === 'reservations' && (
          <div className="library-rules__section">
            <ol className="library-rules__steps">
              <li>{t('เข้าคิวจองได้เมื่อไม่มีตัวเล่มพร้อมให้ยืม และเรียงคิวตามเวลาที่จอง', 'Join the queue when no copies are available. Members are served in reservation order.')}</li>
              <li>{t('เมื่อมีผู้คืนหนังสือ ระบบกันตัวเล่มให้สมาชิกคนแรกในคิวและเปลี่ยนสถานะเป็นพร้อมรับ', 'When a copy is returned, the system holds it for the first member in the queue and marks it ready for pickup.')}</li>
              <li>{t('ต้องมารับภายใน 48 ชั่วโมง ระบบจะแสดงสถานะและเวลาหมดอายุในหน้าโปรไฟล์', 'Pick up the copy within 48 hours. The status and expiry time appear in your profile.')}</li>
              <li>{t('ยกเลิกรายการที่กำลังรอหรือพร้อมรับได้ หากยกเลิกหรือหมดเวลา ระบบจะส่งตัวเล่มให้คิวถัดไป', 'You may cancel a waiting or ready reservation. If it is cancelled or expires, the copy moves to the next member.')}</li>
            </ol>
          </div>
        )}
      </div>
    </section>
  );
}
