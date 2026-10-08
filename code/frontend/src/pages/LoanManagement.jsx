import { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import api from '../api';

export default function LoanManagement() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const { user } = useContext(AuthContext); // 2. ดึงข้อมูล user จาก Context

  // State สำหรับฟอร์มสร้างใบยืมใหม่
  const [borrowData, setBorrowData] = useState({
    memberId: '',
    barcodes: ''
  });

  const fetchLoans = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/v1/loans?size=50');
      setLoans(response.data.content || []);
    } catch (error) {
      console.error("Fetch loans error:", error);
      setMessage({ type: 'error', text: 'ไม่สามารถดึงข้อมูลใบยืมได้' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLoans();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setBorrowData(prev => ({ ...prev, [name]: value }));
  };

  const handleBorrowSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });
    
    // จัดรูปแบบ Payload ให้ตรงกับ BorrowRequest
    const payload = {
      memberId: parseInt(borrowData.memberId),
      barcodes: borrowData.barcodes.split(',').map(b => b.trim()).filter(b => b)
    };

    try {
      await api.post('/api/v1/loans', payload);
      setMessage({ type: 'success', text: 'บันทึกการยืมสำเร็จ' });
      setBorrowData({ memberId: '', barcodes: '' });
      fetchLoans();
    } catch (error) {
      console.error("Borrow error:", error);
      setMessage({ 
        type: 'error', 
        text: error.response?.data?.message || 'เกิดข้อผิดพลาดในการยืม (เช่น โควต้าเต็ม, ติดค่าปรับ, หนังสือไม่ว่าง)' 
      });
    }
  };

  // ฟังก์ชันสำหรับเรียก API คืนหนังสือ หรือ ต่ออายุ
  const handleAction = async (id, action) => {
    try {
      await api.patch(`/api/v1/loans/${id}/${action}`);
      setMessage({ 
        type: 'success', 
        text: action === 'return' ? 'บันทึกการคืนหนังสือสำเร็จ' : 'ต่ออายุใบยืมสำเร็จ' 
      });
      fetchLoans();
    } catch (error) {
      console.error(`${action} error:`, error);
      setMessage({ 
        type: 'error', 
        text: error.response?.data?.message || `เกิดข้อผิดพลาดในการ${action === 'return' ? 'คืน' : 'ต่ออายุ'}` 
      });
    }
  };

  // 3. เพิ่มฟังก์ชันสำหรับ ADMIN ลบใบยืม
  const handleDeleteLoan = async (id) => {
    if (!window.confirm('คุณแน่ใจหรือไม่ที่จะลบใบยืมนี้ออกจากระบบ? (สิทธิ์เฉพาะ ADMIN)')) return;
    try {
      await api.delete(`/api/v1/loans/${id}`);
      setMessage({ type: 'success', text: 'ยกเลิกใบยืมสำเร็จ' });
      fetchLoans();
    } catch (error) {
      console.error("Delete loan error:", error);
      setMessage({ type: 'error', text: 'เกิดข้อผิดพลาด ไม่สามารถลบใบยืมได้' });
    }
  };

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-6xl mx-auto space-y-6">
        
        <h1 className="text-3xl font-bold text-gray-800">จัดการการยืม-คืน</h1>

        {message.text && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {message.text}
          </div>
        )}

        {/* ส่วนฟอร์มยืมหนังสือใหม่ */}
        <div className="p-6 bg-white border shadow-sm rounded-xl">
          <h2 className="mb-4 text-xl font-bold text-gray-800">บันทึกการยืมใหม่</h2>
          <form onSubmit={handleBorrowSubmit} className="flex items-end gap-4">
            <div className="flex-1">
              <label className="block mb-1 text-sm font-medium text-gray-700">รหัสสมาชิก (Member ID)</label>
              <input 
                type="number" 
                name="memberId" 
                value={borrowData.memberId} 
                onChange={handleInputChange} 
                required 
                className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                placeholder="เช่น: 3"
              />
            </div>
            <div className="flex-[2]">
              <label className="block mb-1 text-sm font-medium text-gray-700">บาร์โค้ดหนังสือ (คั่นด้วยลูกน้ำ)</label>
              <input 
                type="text" 
                name="barcodes" 
                value={borrowData.barcodes} 
                onChange={handleInputChange} 
                required 
                className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                placeholder="เช่น: LIB-00231, LIB-00842"
              />
            </div>
            <button 
              type="submit" 
              className="px-6 py-2 font-bold text-white transition bg-blue-600 rounded-md hover:bg-blue-700 h-[42px]"
            >
              ทำรายการยืม
            </button>
          </form>
        </div>

        {/* ส่วนตารางรายการใบยืม */}
        <div className="overflow-hidden bg-white border shadow-sm rounded-xl">
          <div className="p-4 border-b bg-gray-50">
            <h2 className="text-lg font-bold text-gray-800">ประวัติการทำรายการล่าสุด</h2>
          </div>
          {loading ? (
            <div className="py-10 text-center text-gray-500">กำลังโหลดข้อมูล...</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="p-4 text-sm font-semibold text-gray-700">รหัสใบยืม</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">ผู้ยืม</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">วันที่ยืม</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">สถานะ</th>
                    <th className="p-4 text-sm font-semibold text-right text-gray-700">จัดการ</th>
                  </tr>
                </thead>
                <tbody>
                  {loans.map(loan => (
                    <tr key={loan.id} className="border-b hover:bg-gray-50">
                      <td className="p-4 text-sm font-medium text-gray-800">{loan.loanCode}</td>
                      <td className="p-4 text-sm text-gray-600">{loan.memberName} ({loan.memberTier})</td>
                      <td className="p-4 text-sm text-gray-600">{new Date(loan.loanDate).toLocaleDateString('th-TH')}</td>
                      <td className="p-4 text-sm">
                        <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                          loan.status === 'ACTIVE' ? 'bg-blue-100 text-blue-800' :
                          loan.status === 'RETURNED' ? 'bg-green-100 text-green-800' :
                          loan.status === 'OVERDUE' ? 'bg-red-100 text-red-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {loan.status}
                        </span>
                      </td>
                      <td className="p-4 space-x-2 text-sm text-right">
                        {(loan.status === 'ACTIVE' || loan.status === 'OVERDUE') && (
                          <>
                            <button 
                              onClick={() => handleAction(loan.id, 'return')}
                              className="px-3 py-1 text-xs font-semibold text-green-700 bg-green-100 rounded-md hover:bg-green-200"
                            >
                              คืน
                            </button>
                            {loan.status === 'ACTIVE' && (
                              <button 
                                onClick={() => handleAction(loan.id, 'renew')}
                                className="px-3 py-1 text-xs font-semibold text-yellow-700 bg-yellow-100 rounded-md hover:bg-yellow-200"
                              >
                                ต่ออายุ
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
                            ลบใบยืม (Admin)
                        </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {loans.length === 0 && (
                    <tr>
                      <td colSpan="5" className="p-6 text-center text-gray-500">ไม่มีข้อมูลใบยืมในระบบ</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}