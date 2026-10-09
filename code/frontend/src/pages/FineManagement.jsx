import { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContextValue';
import { useLanguage } from '../context/LanguageContext';
import api from '../api';

export default function FineManagement() {
  const [memberId, setMemberId] = useState('');
  const [fines, setFines] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', th: '', en: '' });
  const [hasSearched, setHasSearched] = useState(false);
  const { user } = useContext(AuthContext); // 2. ดึงข้อมูล user จาก Context
  const { t, language } = useLanguage();

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!memberId) return;

    setLoading(true);
    setMessage({ type: '', th: '', en: '' });
    setHasSearched(true);
    
    try {
      // ดึงเฉพาะค่าปรับที่ยังไม่จ่าย (UNPAID) ตาม API Spec
      const response = await api.get(`/api/v1/members/${memberId}/fines?status=UNPAID`);
      setFines(response.data || []);
      if (response.data.length === 0) {
        setMessage({ type: 'success', th: 'สมาชิกรายนี้ไม่มียอดค่าปรับค้างชำระ', en: 'This member has no outstanding fines.' });
      }
    } catch (error) {
      console.error("Fetch fines error:", error);
      setFines([]);
      setMessage({ type: 'error', th: 'ไม่สามารถดึงข้อมูลค่าปรับได้ (ตรวจสอบรหัสสมาชิกอีกครั้ง)', en: 'Could not load fines. Check the member ID and try again.' });
    } finally {
      setLoading(false);
    }
  };

  const handlePayFine = async (fineId) => {
    if (!window.confirm(t('ยืนยันการรับชำระเงินค่าปรับรายการนี้?', 'Confirm payment for this fine?'))) return;

    try {
      // เรียก API ชำระเงิน
      await api.post(`/api/v1/fines/${fineId}/pay`);
      setMessage({ type: 'success', th: `บันทึกการชำระเงินรหัส ${fineId} สำเร็จ`, en: `Payment for fine ${fineId} recorded successfully.` });
      
      // อัปเดต UI โดยเอารายการที่จ่ายแล้วออกจากตาราง
      setFines(prev => prev.filter(fine => fine.id !== fineId));
    } catch (error) {
      console.error("Pay fine error:", error);
      setMessage({ type: 'error', th: 'เกิดข้อผิดพลาดในการบันทึกรับชำระเงิน', en: 'Could not record the fine payment.' });
    }
  };

  // 3. เพิ่มฟังก์ชันสำหรับ ADMIN ยกเว้นค่าปรับ
  const handleWaiveFine = async (fineId) => {
    if (!window.confirm(t('ยืนยันการยกเว้นค่าปรับรายการนี้? (สิทธิ์เฉพาะ ADMIN)', 'Waive this fine? (ADMIN only)'))) return;
    try {
      await api.post(`/api/v1/fines/${fineId}/waive`);
      setMessage({ type: 'success', th: `ยกเว้นค่าปรับรหัส ${fineId} สำเร็จ`, en: `Fine ${fineId} waived successfully.` });
      setFines(prev => prev.filter(fine => fine.id !== fineId)); // อัปเดตตาราง
    } catch (error) {
      console.error("Waive fine error:", error);
      setMessage({ type: 'error', th: 'เกิดข้อผิดพลาดในการยกเว้นค่าปรับ', en: 'Could not waive the fine.' });
    }
  };

  // รวมยอดค่าปรับที่ค้างชำระทั้งหมด
  const totalUnpaid = fines.reduce((sum, fine) => sum + (fine.amount || 0), 0);

  return (
    <main className="lf-workspace-page">
      <div className="lf-workspace-content max-w-4xl mx-auto">
        
        <h1 className="text-3xl font-bold text-gray-800">{t('จัดการค่าปรับ', 'Manage fines')}</h1>

        {/* ฟอร์มค้นหาผู้ใช้ */}
        <div className="p-6 bg-white border shadow-xs rounded-xl">
          <form onSubmit={handleSearch} className="flex items-end gap-4">
            <div className="flex-1">
              <label className="block mb-1 text-sm font-medium text-gray-700">{t('รหัสสมาชิก (Member ID)', 'Member ID')}</label>
              <input 
                type="number" 
                value={memberId} 
                onChange={(e) => setMemberId(e.target.value)} 
                required 
                className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                placeholder={t('กรอกรหัสสมาชิกเพื่อตรวจสอบยอดค้างชำระ', 'Enter a member ID to check outstanding fines')}
              />
            </div>
            <button 
              type="submit" 
              className="px-6 py-2 font-bold text-white transition bg-gray-800 rounded-md hover:bg-gray-900 h-[42px]"
            >
              {t('ค้นหาค่าปรับ', 'Search fines')}
            </button>
          </form>
        </div>

        {(message.th || message.en) && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {t(message.th, message.en)}
          </div>
        )}

        {/* ตารางแสดงค่าปรับ */}
        {hasSearched && (
          <div className="overflow-hidden bg-white border shadow-xs rounded-xl">
            <div className="flex items-center justify-between p-4 border-b bg-gray-50">
              <h2 className="text-lg font-bold text-gray-800">{t('รายการค่าปรับค้างชำระ', 'Outstanding fines')} ({t('ค้างชำระ', 'UNPAID')})</h2>
              {fines.length > 0 && (
                <span className="font-bold text-red-600">
                  {t('ยอดรวมทั้งหมด:', 'Total:')} {totalUnpaid.toLocaleString(language === 'th' ? 'th-TH' : 'en-US', { minimumFractionDigits: 2 })} {t('บาท', 'THB')}
                </span>
              )}
            </div>
            
            {loading ? (
              <div className="py-10 text-center text-gray-500">{t('กำลังโหลดข้อมูล...', 'Loading fines...')}</div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-white border-b">
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('รหัสค่าปรับ', 'Fine ID')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('รหัสรายการยืม', 'Loan item ID')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('จำนวนวันเกินกำหนด', 'Days overdue')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('ยอดเงิน', 'Amount')} ({t('บาท', 'THB')})</th>
                    <th className="p-4 text-sm font-semibold text-right text-gray-700">{t('จัดการ', 'Actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {fines.map(fine => (
                    <tr key={fine.id} className="border-b hover:bg-gray-50">
                      <td className="p-4 text-sm text-gray-600">{fine.id}</td>
                      <td className="p-4 text-sm text-gray-600">{fine.loanItemId}</td>
                      <td className="p-4 text-sm text-red-600">{fine.overdueDays} {t('วัน', 'days')}</td>
                      <td className="p-4 text-sm font-bold text-gray-800">{fine.amount.toLocaleString(language === 'th' ? 'th-TH' : 'en-US', { minimumFractionDigits: 2 })}</td>
                      <td className="p-4 text-right">
                        <button 
                          onClick={() => handlePayFine(fine.id)}
                          className="px-4 py-1 text-sm font-bold text-white bg-blue-600 rounded-md hover:bg-blue-700"
                        >
                          {t('รับชำระเงิน', 'Record payment')}
                        </button>

                        {/* 4. เพิ่มปุ่มยกเว้นค่าปรับที่แสดงเฉพาะ ADMIN เท่านั้น */}
                        {user?.role === 'ADMIN' && (
                        <button 
                            onClick={() => handleWaiveFine(fine.id)}
                            className="px-4 py-1 ml-2 text-sm font-bold text-white bg-gray-600 rounded-md hover:bg-gray-700"
                        >
                            {t('ยกเว้น (Admin)', 'Waive (Admin)')}
                        </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {fines.length === 0 && !message.th && !message.en && (
                    <tr><td colSpan="5" className="p-6 text-center text-gray-500">{t('ไม่พบรายการค้างชำระ', 'No outstanding fines found.')}</td></tr>
                  )}
                </tbody>
              </table>
            )}
          </div>
        )}

      </div>
    </main>
  );
}
