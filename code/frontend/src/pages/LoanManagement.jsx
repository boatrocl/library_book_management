import { useCallback, useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContextValue';
import { useLanguage } from '../context/LanguageContext';
import api from '../api';

function formatDate(value, language) {
  if (!value) return '-';
  const date = new Date(`${String(value).slice(0, 10)}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(language === 'th' ? 'th-TH' : 'en-US', { dateStyle: 'medium' }).format(date);
}

export default function LoanManagement() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', th: '', en: '' });
  const { user } = useContext(AuthContext); // 2. ดึงข้อมูล user จาก Context
  const { language, t, enumLabel } = useLanguage();

  // State สำหรับฟอร์มสร้างใบยืมใหม่
  const [borrowData, setBorrowData] = useState({
    memberId: '',
    barcodes: ''
  });

  const fetchLoans = useCallback(async (signal) => {
    try {
      const response = await api.get('/api/v1/loans?size=50', { signal });
      if (signal?.aborted) return;
      setLoans(response.data.content || []);
    } catch (error) {
      if (signal?.aborted) return;
      console.error("Fetch loans error:", error);
      setMessage({ type: 'error', th: 'ไม่สามารถดึงข้อมูลใบยืมได้', en: 'Could not load loans.' });
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void Promise.resolve().then(() => {
      if (!controller.signal.aborted) return fetchLoans(controller.signal);
    });
    return () => controller.abort();
  }, [fetchLoans]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setBorrowData(prev => ({ ...prev, [name]: value }));
  };

  const handleBorrowSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', th: '', en: '' });
    
    // จัดรูปแบบ Payload ให้ตรงกับ BorrowRequest
    const payload = {
      memberId: parseInt(borrowData.memberId),
      barcodes: borrowData.barcodes.split(',').map(b => b.trim()).filter(b => b)
    };

    try {
      await api.post('/api/v1/loans', payload);
      setMessage({ type: 'success', th: 'บันทึกการยืมสำเร็จ', en: 'Loan recorded successfully.' });
      setBorrowData({ memberId: '', barcodes: '' });
      setLoading(true);
      await fetchLoans();
    } catch (error) {
      console.error("Borrow error:", error);
      setMessage({ type: 'error', th: error.response?.data?.message || 'เกิดข้อผิดพลาดในการยืม (เช่น โควต้าเต็ม, ติดค่าปรับ, หนังสือไม่ว่าง)', en: 'Could not create the loan. Check the member limit, unpaid fines, and book availability.' });
    }
  };

  // ฟังก์ชันสำหรับเรียก API คืนหนังสือ หรือ ต่ออายุ
  const handleAction = async (id, action) => {
    try {
      await api.patch(`/api/v1/loans/${id}/${action}`);
      setMessage({
        type: 'success',
        th: action === 'return' ? 'บันทึกการคืนหนังสือสำเร็จ' : 'ต่ออายุใบยืมสำเร็จ',
        en: action === 'return' ? 'Book return recorded successfully.' : 'Loan renewed successfully.',
      });
      setLoading(true);
      await fetchLoans();
    } catch (error) {
      console.error(`${action} error:`, error);
      setMessage({
        type: 'error',
        th: error.response?.data?.message || `เกิดข้อผิดพลาดในการ${action === 'return' ? 'คืน' : 'ต่ออายุ'}`,
        en: action === 'return' ? 'Could not record the book return.' : 'Could not renew the loan.',
      });
    }
  };

  // 3. เพิ่มฟังก์ชันสำหรับ ADMIN ลบใบยืม
  const handleDeleteLoan = async (id) => {
    if (!window.confirm(t('คุณแน่ใจหรือไม่ที่จะลบใบยืมนี้ออกจากระบบ? (สิทธิ์เฉพาะ ADMIN)', 'Are you sure you want to delete this loan? (ADMIN only)'))) return;
    try {
      await api.delete(`/api/v1/loans/${id}`);
      setMessage({ type: 'success', th: 'ยกเลิกใบยืมสำเร็จ', en: 'Loan cancelled successfully.' });
      setLoading(true);
      await fetchLoans();
    } catch (error) {
      console.error("Delete loan error:", error);
      setMessage({ type: 'error', th: 'เกิดข้อผิดพลาด ไม่สามารถลบใบยืมได้', en: 'Could not delete the loan.' });
    }
  };

  return (
    <main className="lf-workspace-page">
      <div className="lf-workspace-content max-w-6xl mx-auto">
        
        <h1 className="text-3xl font-bold text-gray-800">{t('จัดการการยืม-คืน', 'Manage loans and returns')}</h1>

        {(message.th || message.en) && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {t(message.th, message.en)}
          </div>
        )}

        {/* ส่วนฟอร์มยืมหนังสือใหม่ */}
        <div className="p-6 bg-white border shadow-xs rounded-xl">
          <h2 className="mb-4 text-xl font-bold text-gray-800">{t('บันทึกการยืมใหม่', 'Create a loan')}</h2>
          <form onSubmit={handleBorrowSubmit} className="flex items-end gap-4">
            <div className="flex-1">
              <label className="block mb-1 text-sm font-medium text-gray-700">{t('รหัสสมาชิก (Member ID)', 'Member ID')}</label>
              <input 
                type="number" 
                name="memberId" 
                value={borrowData.memberId} 
                onChange={handleInputChange} 
                required 
                className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                placeholder={t('เช่น: 3', 'e.g. 3')}
              />
            </div>
            <div className="flex-2">
              <label className="block mb-1 text-sm font-medium text-gray-700">{t('บาร์โค้ดหนังสือ (คั่นด้วยลูกน้ำ)', 'Book barcodes (comma-separated)')}</label>
              <input 
                type="text" 
                name="barcodes" 
                value={borrowData.barcodes} 
                onChange={handleInputChange} 
                required 
                className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                placeholder="e.g. LIB-00231, LIB-00842"
              />
            </div>
            <button 
              type="submit" 
              className="px-6 py-2 font-bold text-white transition bg-blue-600 rounded-md hover:bg-blue-700 h-[42px]"
            >
              {t('ทำรายการยืม', 'Create loan')}
            </button>
          </form>
        </div>

        {/* ส่วนตารางรายการใบยืม */}
        <div className="overflow-hidden bg-white border shadow-xs rounded-xl">
          <div className="p-4 border-b bg-gray-50">
            <h2 className="text-lg font-bold text-gray-800">{t('ประวัติการทำรายการล่าสุด', 'Recent loan activity')}</h2>
          </div>
          {loading ? (
            <div className="py-10 text-center text-gray-500">{t('กำลังโหลดข้อมูล...', 'Loading loans...')}</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1050px] table-fixed text-left border-collapse">
                <colgroup>
                  <col className="w-[12%]" />
                  <col className="w-[17%]" />
                  <col className="w-[12%]" />
                  <col className="w-[24%]" />
                  <col className="w-[12%]" />
                  <col className="w-[23%]" />
                </colgroup>
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="whitespace-nowrap p-4 text-sm font-semibold text-gray-700">{t('รหัสใบยืม', 'Loan ID')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('ผู้ยืม', 'Member')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('วันที่ยืม', 'Loan date')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('หนังสือ / กำหนดคืน', 'Book / due')}</th>
                    <th className="whitespace-nowrap p-4 text-sm font-semibold text-gray-700">{t('สถานะ', 'Status')}</th>
                    <th className="p-4 text-sm font-semibold text-right text-gray-700">{t('จัดการ', 'Actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {loans.map(loan => (
                    <tr key={loan.id} className="border-b hover:bg-gray-50">
                      <td className="whitespace-nowrap p-4 text-sm font-medium text-gray-800">{loan.loanCode}</td>
                      <td className="p-4 text-sm text-gray-600">{loan.memberName} ({enumLabel(loan.memberTier)})</td>
                      <td className="whitespace-nowrap p-4 text-sm text-gray-600">{formatDate(loan.loanDate, language)}</td>
                      <td className="p-4 text-sm text-gray-700">
                        {loan.items?.length ? (
                          <ul className="space-y-2">
                            {loan.items.map((item) => (
                              <li key={item.id} className="border-l-2 border-teal-200 pl-3">
                                <span className="block max-w-[12rem] break-words font-medium text-slate-800">{item.bookTitle || item.barcode}</span>
                                <span className="block whitespace-normal break-words text-xs text-slate-600">
                                  {t('กำหนดคืน', 'Due')}: {formatDate(item.dueDate, language)}
                                  {item.returnedAt && <> · {t('คืนแล้ว', 'Returned')}: {formatDate(item.returnedAt, language)}</>}
                                </span>
                              </li>
                            ))}
                          </ul>
                        ) : <span className="text-slate-500">-</span>}
                      </td>
                      <td className="whitespace-nowrap p-4 text-sm">
                        <span className={`inline-flex whitespace-nowrap px-2 py-1 text-xs font-bold rounded-full ${
                          loan.status === 'ACTIVE' ? 'bg-blue-100 text-blue-800' :
                          loan.status === 'RETURNED' ? 'bg-green-100 text-green-800' :
                          loan.status === 'OVERDUE' ? 'bg-red-100 text-red-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {enumLabel(loan.status)}
                        </span>
                      </td>
                      <td className="p-4 space-x-2 text-sm text-right">
                        {(loan.status === 'ACTIVE' || loan.status === 'OVERDUE') && (
                          <>
                            <button 
                              onClick={() => handleAction(loan.id, 'return')}
                              className="px-3 py-1 text-xs font-semibold text-green-700 bg-green-100 rounded-md hover:bg-green-200"
                            >
                              {t('คืน', 'Return')}
                            </button>
                            {loan.status === 'ACTIVE' && (
                              <button 
                                onClick={() => handleAction(loan.id, 'renew')}
                                className="px-3 py-1 text-xs font-semibold text-yellow-700 bg-yellow-100 rounded-md hover:bg-yellow-200"
                              >
                                {t('ต่ออายุ', 'Renew')}
                              </button>
                            )}
                          </>
                        )}

                        {/* 4. เพิ่มปุ่มลบใบยืมที่แสดงเฉพาะ ADMIN เท่านั้น */}
                        {user?.role === 'ADMIN' && (
                        <button 
                            onClick={() => handleDeleteLoan(loan.id)}
                            className="px-3 py-1 text-xs font-semibold text-red-700 bg-red-100 rounded-md hover:bg-red-200"
                        >
                            {t('ลบใบยืม (Admin)', 'Delete loan (Admin)')}
                        </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {loans.length === 0 && (
                    <tr>
                      <td colSpan="6" className="p-6 text-center text-gray-500">{t('ไม่มีข้อมูลใบยืมในระบบ', 'No loans found.')}</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </main>
  );
}
