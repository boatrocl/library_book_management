import { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';

export default function BookCopyManagement() {
  const { id } = useParams(); // รับ ID หนังสือจาก URL
  const navigate = useNavigate();
  const [copies, setCopies] = useState([]);
  const [bookTitle, setBookTitle] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });

  const fetchData = useCallback(async (signal) => {
    try {
      const [bookRes, copiesRes] = await Promise.all([
        api.get(`/api/v1/books/${id}`, { signal }),
        api.get(`/api/v1/books/${id}/copies`, { signal })
      ]);
      if (signal?.aborted) return;

      setBookTitle(bookRes.data.title);
      setCopies(copiesRes.data);
    } catch (error) {
      if (signal?.aborted) return;
      console.error("Fetch copies error:", error);
      setMessage({ type: 'error', text: 'ไม่สามารถดึงข้อมูลตัวเล่มได้' });
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    const controller = new AbortController();
    void Promise.resolve().then(() => {
      if (!controller.signal.aborted) return fetchData(controller.signal);
    });
    return () => controller.abort();
  }, [fetchData]);

  const handleAddCopy = async () => {
    try {
      // โค้ด Backend ของเพื่อนรองรับการสร้างบาร์โค้ดอัตโนมัติเมื่อส่งข้อมูลว่างไป
      await api.post(`/api/v1/books/${id}/copies`, { shelfLocation: 'General' });
      setMessage({ type: 'success', text: 'เพิ่มตัวเล่มหนังสือใหม่เข้าคลังสำเร็จ' });
      setLoading(true);
      await fetchData(); // โหลดตารางใหม่
    } catch (error) {
      console.error("Add copy error:", error);
      setMessage({ type: 'error', text: 'ไม่สามารถเพิ่มตัวเล่มได้' });
    }
  };

  return (
    <main className="lf-workspace-page">
      <div className="lf-workspace-content max-w-4xl mx-auto">
        
        <div className="flex items-center space-x-4">
          <button onClick={() => navigate('/admin/books')} className="px-4 py-2 font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300">
            ← กลับไปหน้าหนังสือ
          </button>
          <h1 className="text-2xl font-bold text-gray-800">จัดการตัวเล่ม: {bookTitle}</h1>
        </div>

        {message.text && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {message.text}
          </div>
        )}

        <div className="overflow-hidden bg-white border shadow-xs rounded-xl">
          <div className="flex items-center justify-between p-4 border-b bg-gray-50">
            <h2 className="text-lg font-bold text-gray-800">รายการตัวเล่มทั้งหมด</h2>
            <button onClick={handleAddCopy} className="px-4 py-2 text-sm font-bold text-white transition bg-green-600 rounded-md hover:bg-green-700">
              + เพิ่มตัวเล่มใหม่
            </button>
          </div>
          
          {loading ? (
            <div className="py-10 text-center text-gray-500">กำลังโหลดข้อมูล...</div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-white border-b">
                  <th className="p-4 text-sm font-semibold text-gray-700">บาร์โค้ด (Barcode)</th>
                  <th className="p-4 text-sm font-semibold text-gray-700">สถานะ</th>
                  <th className="p-4 text-sm font-semibold text-gray-700">ตำแหน่งชั้นวาง</th>
                </tr>
              </thead>
              <tbody>
                {copies.map(copy => (
                  <tr key={copy.id} className="border-b hover:bg-gray-50">
                    <td className="p-4 font-mono text-sm font-medium text-blue-600">{copy.barcode}</td>
                    <td className="p-4 text-sm">
                      <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                        copy.status === 'AVAILABLE' ? 'bg-green-100 text-green-800' : 
                        copy.status === 'ON_LOAN' ? 'bg-yellow-100 text-yellow-800' : 
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {copy.status}
                      </span>
                    </td>
                    <td className="p-4 text-sm text-gray-600">{copy.shelfLocation || '-'}</td>
                  </tr>
                ))}
                {copies.length === 0 && (
                  <tr><td colSpan="3" className="p-6 text-center text-gray-500">ยังไม่มีตัวเล่มในระบบ</td></tr>
                )}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </main>
  );
}
