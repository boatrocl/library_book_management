import { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import api from '../api';

export default function FineManagement() {
  const [memberId, setMemberId] = useState('');
  const [fines, setFines] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [hasSearched, setHasSearched] = useState(false);
  const { user } = useContext(AuthContext); // 2. ดึงข้อมูล user จาก Context

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!memberId) return;

    setLoading(true);
    setMessage({ type: '', text: '' });
    setHasSearched(true);
    
    try {
      // ดึงเฉพาะค่าปรับที่ยังไม่จ่าย (UNPAID) ตาม API Spec
      const response = await api.get(`/api/v1/members/${memberId}/fines?status=UNPAID`);
      setFines(response.data || []);
      if (response.data.length === 0) {
        setMessage({ type: 'success', text: 'สมาชิกรายนี้ไม่มียอดค่าปรับค้างชำระ' });
      }
    } catch (error) {
      console.error("Fetch fines error:", error);
      setFines([]);
      setMessage({ type: 'error', text: 'ไม่สามารถดึงข้อมูลค่าปรับได้ (ตรวจสอบรหัสสมาชิกอีกครั้ง)' });
    } finally {
      setLoading(false);
    }
  };

  const handlePayFine = async (fineId) => {
    if (!window.confirm('ยืนยันการรับชำระเงินค่าปรับรายการนี้?')) return;

    try {
      // เรียก API ชำระเงิน
      await api.post(`/api/v1/fines/${fineId}/pay`);
      setMessage({ type: 'success', text: `บันทึกการชำระเงินรหัส ${fineId} สำเร็จ` });
      
      // อัปเดต UI โดยเอารายการที่จ่ายแล้วออกจากตาราง
      setFines(prev => prev.filter(fine => fine.id !== fineId));
    } catch (error) {
      console.error("Pay fine error:", error);
      setMessage({ type: 'error', text: 'เกิดข้อผิดพลาดในการบันทึกรับชำระเงิน' });
    }
  };

  // 3. เพิ่มฟังก์ชันสำหรับ ADMIN ยกเว้นค่าปรับ
  const handleWaiveFine = async (fineId) => {
    if (!window.confirm('ยืนยันการยกเว้นค่าปรับ (Waive) รายการนี้? (สิทธิ์เฉพาะ ADMIN)')) return;
    try {
      await api.post(`/api/v1/fines/${fineId}/waive`);
      setMessage({ type: 'success', text: `ยกเว้นค่าปรับรหัส ${fineId} สำเร็จ` });
      setFines(prev => prev.filter(fine => fine.id !== fineId)); // อัปเดตตาราง
    } catch (error) {
      console.error("Waive fine error:", error);
      setMessage({ type: 'error', text: 'เกิดข้อผิดพลาดในการยกเว้นค่าปรับ' });
    }
  };

  // รวมยอดค่าปรับที่ค้างชำระทั้งหมด
  const totalUnpaid = fines.reduce((sum, fine) => sum + (fine.amount || 0), 0);

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-4xl mx-auto space-y-6">
        
        <h1 className="text-3xl font-bold text-gray-800">จัดการค่าปรับ</h1>

        {/* ฟอร์มค้นหาผู้ใช้ */}
        <div className="p-6 bg-white border shadow-sm rounded-xl">
          <form onSubmit={handleSearch} className="flex items-end gap-4">
            <div className="flex-1">
              <label className="block mb-1 text-sm font-medium text-gray-700">รหัสสมาชิก (Member ID)</label>
              <input 
                type="number" 
                value={memberId} 
                onChange={(e) => setMemberId(e.target.value)} 
                required 
                className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                placeholder="กรอกรหัสสมาชิกเพื่อตรวจสอบยอดค้างชำระ"
              />
            </div>
            <button 
              type="submit" 
              className="px-6 py-2 font-bold text-white transition bg-gray-800 rounded-md hover:bg-gray-900 h-[42px]"
            >
              ค้นหาค่าปรับ
            </button>
          </form>
        </div>

        {message.text && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {message.text}
          </div>
        )}

        {/* ตารางแสดงค่าปรับ */}
        {hasSearched && (
          <div className="overflow-hidden bg-white border shadow-sm rounded-xl">
            <div className="flex items-center justify-between p-4 border-b bg-gray-50">
              <h2 className="text-lg font-bold text-gray-800">รายการค่าปรับค้างชำระ (UNPAID)</h2>
              {fines.length > 0 && (
                <span className="font-bold text-red-600">
                  ยอดรวมทั้งหมด: {totalUnpaid.toFixed(2)} บาท
                </span>
              )}
            </div>
            
            {loading ? (
              <div className="py-10 text-center text-gray-500">กำลังโหลดข้อมูล...</div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-white border-b">
                    <th className="p-4 text-sm font-semibold text-gray-700">รหัสค่าปรับ</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">รหัสการยืม (Loan Item ID)</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">จำนวนวันเกินกำหนด</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">ยอดเงิน (บาท)</th>
                    <th className="p-4 text-sm font-semibold text-right text-gray-700">จัดการ</th>
                  </tr>
                </thead>
                <tbody>
                  {fines.map(fine => (
                    <tr key={fine.id} className="border-b hover:bg-gray-50">
                      <td className="p-4 text-sm text-gray-600">{fine.id}</td>
                      <td className="p-4 text-sm text-gray-600">{fine.loanItemId}</td>
                      <td className="p-4 text-sm text-red-600">{fine.overdueDays} วัน</td>
                      <td className="p-4 text-sm font-bold text-gray-800">{fine.amount.toFixed(2)}</td>
                      <td className="p-4 text-right">
                        <button 
                          onClick={() => handlePayFine(fine.id)}
                          className="px-4 py-1 text-sm font-bold text-white bg-blue-600 rounded-md hover:bg-blue-700"
                        >
                          รับชำระเงิน
                        </button>

                        {/* 4. เพิ่มปุ่มยกเว้นค่าปรับที่แสดงเฉพาะ ADMIN เท่านั้น */}
                        {user?.role === 'ADMIN' && (
                        <button 
                            onClick={() => handleWaiveFine(fine.id)}
                            className="px-4 py-1 ml-2 text-sm font-bold text-white bg-gray-600 rounded-md hover:bg-gray-700"
                        >
                            ยกเว้น (Admin)
                        </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {fines.length === 0 && !message.text && (
                    <tr><td colSpan="5" className="p-6 text-center text-gray-500">ไม่พบรายการค้างชำระ</td></tr>
                  )}
                </tbody>
              </table>
            )}
          </div>
        )}

      </div>
    </div>
  );
}