import { useState } from 'react';
import api from '../api';

export default function ReportManagement() {
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [loadingCsv, setLoadingCsv] = useState(false);
  const [loadingPdf, setLoadingPdf] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const downloadReport = async (endpoint, filename, type) => {
    try {
      if (type === 'CSV') setLoadingCsv(true);
      if (type === 'PDF') setLoadingPdf(true);
      setMessage({ type: '', text: '' });

      // ต้องกำหนด responseType เป็น blob เพื่อรับไฟล์แทน JSON
      const response = await api.get(endpoint, { responseType: 'blob' });
      
      // สร้าง URL สำหรับดาวน์โหลดไฟล์จำลองบนเบราว์เซอร์
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      
      setMessage({ type: 'success', text: `ดาวน์โหลด ${filename} สำเร็จ` });
    } catch (error) {
      console.error("Download error:", error);
      setMessage({ type: 'error', text: 'เกิดข้อผิดพลาด ไม่สามารถดาวน์โหลดรายงานได้ (อาจยังไม่มีข้อมูลในระบบ)' });
    } finally {
      setLoadingCsv(false);
      setLoadingPdf(false);
    }
  };

  const handleDownloadCsv = (e) => {
    e.preventDefault();
    if (!fromDate || !toDate) {
      setMessage({ type: 'error', text: 'กรุณาระบุช่วงวันที่เริ่มต้นและสิ้นสุด' });
      return;
    }
    const endpoint = `/api/v1/reports/loans?from=${fromDate}&to=${toDate}&format=CSV`;
    downloadReport(endpoint, `loan_report_${fromDate}_to_${toDate}.csv`, 'CSV');
  };

  const handleDownloadPdf = () => {
    const endpoint = `/api/v1/reports/overdue?format=PDF`;
    downloadReport(endpoint, 'overdue_report.pdf', 'PDF');
  };

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-4xl mx-auto space-y-6">
        
        <h1 className="text-3xl font-bold text-gray-800">ออกรายงาน</h1>

        {message.text && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {message.text}
          </div>
        )}

        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          
          {/* Card 1: รายงานสถิติการยืม (CSV) */}
          <div className="p-6 bg-white border shadow-sm rounded-xl">
            <h2 className="mb-4 text-xl font-bold text-gray-800">สถิติการยืม (CSV)</h2>
            <p className="mb-4 text-sm text-gray-600">ดาวน์โหลดข้อมูลการยืมหนังสือทั้งหมดตามช่วงวันที่กำหนด</p>
            <form onSubmit={handleDownloadCsv} className="space-y-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">วันที่เริ่มต้น (From)</label>
                <input 
                  type="date" 
                  value={fromDate} 
                  onChange={(e) => setFromDate(e.target.value)} 
                  required 
                  className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">วันที่สิ้นสุด (To)</label>
                <input 
                  type="date" 
                  value={toDate} 
                  onChange={(e) => setToDate(e.target.value)} 
                  required 
                  className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                />
              </div>
              <button 
                type="submit" 
                disabled={loadingCsv}
                className="w-full px-4 py-2 font-bold text-white transition bg-green-600 rounded-md hover:bg-green-700 disabled:opacity-50"
              >
                {loadingCsv ? 'กำลังเตรียมไฟล์...' : 'ดาวน์โหลด CSV'}
              </button>
            </form>
          </div>

          {/* Card 2: รายงานหนังสือค้างส่ง (PDF) */}
          <div className="p-6 bg-white border shadow-sm rounded-xl">
            <h2 className="mb-4 text-xl font-bold text-gray-800">หนังสือค้างส่ง (PDF)</h2>
            <p className="mb-4 text-sm text-gray-600">ดาวน์โหลดสรุปรายการใบยืมที่เลยกำหนดและยังไม่ได้คืนทั้งหมด (สถานะ OVERDUE)</p>
            <div className="mt-8">
              <button 
                onClick={handleDownloadPdf}
                disabled={loadingPdf}
                className="w-full px-4 py-2 font-bold text-white transition bg-red-600 rounded-md hover:bg-red-700 disabled:opacity-50"
              >
                {loadingPdf ? 'กำลังเตรียมไฟล์...' : 'ดาวน์โหลด PDF'}
              </button>
            </div>
          </div>

        </div>

      </div>
    </div>
  );
}