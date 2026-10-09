import { useState } from 'react';
import api from '../api';
import { useLanguage } from '../context/LanguageContext';

export default function ReportManagement() {
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [loadingCsv, setLoadingCsv] = useState(false);
  const [loadingPdf, setLoadingPdf] = useState(false);
  const [message, setMessage] = useState({ type: '', th: '', en: '' });
  const { t } = useLanguage();

  const downloadReport = async (endpoint, filename, type) => {
    try {
      if (type === 'CSV') setLoadingCsv(true);
      if (type === 'PDF') setLoadingPdf(true);
      setMessage({ type: '', th: '', en: '' });

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
      
      setMessage({ type: 'success', th: `ดาวน์โหลด ${filename} สำเร็จ`, en: `Downloaded ${filename} successfully.` });
    } catch (error) {
      console.error("Download error:", error);
      setMessage({ type: 'error', th: 'เกิดข้อผิดพลาด ไม่สามารถดาวน์โหลดรายงานได้ (อาจยังไม่มีข้อมูลในระบบ)', en: 'Could not download the report. There may be no data for this report yet.' });
    } finally {
      setLoadingCsv(false);
      setLoadingPdf(false);
    }
  };

  const handleDownloadCsv = (e) => {
    e.preventDefault();
    if (!fromDate || !toDate) {
      setMessage({ type: 'error', th: 'กรุณาระบุช่วงวันที่เริ่มต้นและสิ้นสุด', en: 'Enter both a start date and an end date.' });
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
    <main className="lf-workspace-page">
      <div className="lf-workspace-content max-w-4xl mx-auto">
        
        <h1 className="text-3xl font-bold text-gray-800">{t('ออกรายงาน', 'Reports')}</h1>

        {(message.th || message.en) && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {t(message.th, message.en)}
          </div>
        )}

        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          
          {/* Card 1: รายงานสถิติการยืม (CSV) */}
          <div className="p-6 bg-white border shadow-xs rounded-xl">
            <h2 className="mb-4 text-xl font-bold text-gray-800">{t('สถิติการยืม (CSV)', 'Loan statistics (CSV)')}</h2>
            <p className="mb-4 text-sm text-gray-600">{t('ดาวน์โหลดข้อมูลการยืมหนังสือทั้งหมดตามช่วงวันที่กำหนด', 'Download all loan records for a selected date range.')}</p>
            <form onSubmit={handleDownloadCsv} className="space-y-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">{t('วันที่เริ่มต้น', 'Start date')}</label>
                <input 
                  type="date" 
                  value={fromDate} 
                  onChange={(e) => setFromDate(e.target.value)} 
                  required 
                  className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" 
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">{t('วันที่สิ้นสุด', 'End date')}</label>
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
                {loadingCsv ? t('กำลังเตรียมไฟล์...', 'Preparing file...') : t('ดาวน์โหลด CSV', 'Download CSV')}
              </button>
            </form>
          </div>

          {/* Card 2: รายงานหนังสือค้างส่ง (PDF) */}
          <div className="p-6 bg-white border shadow-xs rounded-xl">
            <h2 className="mb-4 text-xl font-bold text-gray-800">{t('หนังสือค้างส่ง (PDF)', 'Overdue loans (PDF)')}</h2>
            <p className="mb-4 text-sm text-gray-600">{t('ดาวน์โหลดสรุปรายการใบยืมที่เลยกำหนดและยังไม่ได้คืนทั้งหมด (สถานะ OVERDUE)', 'Download a summary of all loans that are overdue and have not been returned.')}</p>
            <div className="mt-8">
              <button 
                onClick={handleDownloadPdf}
                disabled={loadingPdf}
                className="w-full px-4 py-2 font-bold text-white transition bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {loadingPdf ? t('กำลังเตรียมไฟล์...', 'Preparing file...') : t('ดาวน์โหลด PDF', 'Download PDF')}
              </button>
            </div>
          </div>

        </div>

      </div>
    </main>
  );
}
