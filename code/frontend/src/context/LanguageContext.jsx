import { createContext, useCallback, useContext } from 'react';

export const LanguageContext = createContext({
  language: 'th',
  setLanguage: () => {},
});

const CATEGORY_NAMES = {
  'artificial intelligence': ['ปัญญาประดิษฐ์', 'Artificial Intelligence'],
  business: ['ธุรกิจ', 'Business'],
  'computer networks': ['เครือข่ายคอมพิวเตอร์', 'Computer Networks'],
  database: ['ฐานข้อมูล', 'Database'],
  literature: ['วรรณกรรม', 'Literature'],
  mathematics: ['คณิตศาสตร์', 'Mathematics'],
  'programming languages': ['ภาษาโปรแกรม', 'Programming Languages'],
  'software engineering': ['วิศวกรรมซอฟต์แวร์', 'Software Engineering'],
};

const ENUM_LABELS = {
  ACTIVE: ['กำลังใช้งาน', 'Active'],
  AVAILABLE: ['พร้อมให้ยืม', 'Available'],
  CANCELLED: ['ยกเลิกแล้ว', 'Cancelled'],
  COMPLETED: ['เสร็จสิ้น', 'Completed'],
  DAMAGED: ['ชำรุด', 'Damaged'],
  EXPIRED: ['หมดอายุ', 'Expired'],
  EXTERNAL: ['บุคคลภายนอก', 'External'],
  FULFILLED: ['ดำเนินการแล้ว', 'Fulfilled'],
  LIBRARIAN: ['บรรณารักษ์', 'Librarian'],
  LOST: ['สูญหาย', 'Lost'],
  MEMBER: ['สมาชิก', 'Member'],
  ADMIN: ['ผู้ดูแลระบบ', 'Administrator'],
  ON_LOAN: ['กำลังถูกยืม', 'On loan'],
  OVERDUE: ['เกินกำหนด', 'Overdue'],
  PAID: ['ชำระแล้ว', 'Paid'],
  READY: ['พร้อมรับหนังสือ', 'Ready for pickup'],
  RESERVED: ['ถูกจอง', 'Reserved'],
  RETURNED: ['คืนแล้ว', 'Returned'],
  STUDENT: ['นักเรียน/นักศึกษา', 'Student'],
  STAFF: ['บุคลากร', 'Staff'],
  SUSPENDED: ['ถูกระงับ', 'Suspended'],
  UNAVAILABLE: ['ไม่พร้อมให้ยืม', 'Unavailable'],
  UNPAID: ['ค้างชำระ', 'Unpaid'],
  WAIVED: ['ยกเว้นแล้ว', 'Waived'],
  WAITING: ['กำลังรอ', 'Waiting'],
};

export function useLanguage() {
  const { language, setLanguage } = useContext(LanguageContext);
  const t = useCallback((thai, english) => language === 'en' ? english : thai, [language]);
  const categoryName = useCallback((name) => {
    const entry = CATEGORY_NAMES[String(name || '').trim().toLowerCase()];
    return entry ? entry[language === 'en' ? 1 : 0] : name;
  }, [language]);
  const enumLabel = useCallback((value) => {
    const entry = ENUM_LABELS[String(value || '').toUpperCase()];
    return entry ? entry[language === 'en' ? 1 : 0] : value;
  }, [language]);

  return { language, setLanguage, t, categoryName, enumLabel };
}
