import { useState, useEffect } from 'react';
import api from '../api';

export default function Catalog() {
  const [books, setBooks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  const fetchBooks = async (currentPage) => {
    setLoading(true);
    try {
      // ยิง API ดึงข้อมูลหนังสือ พร้อมกำหนดหน้า (page) และจำนวนต่อหน้า (size)
      const response = await api.get(`/api/v1/books?page=${currentPage}&size=8`);
      
      // นำข้อมูล content ที่ได้จาก PageResponse มาเก็บใน state
      setBooks(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("เกิดข้อผิดพลาดในการดึงข้อมูลหนังสือ", error);
    } finally {
      setLoading(false);
    }
  };

  // เรียกใช้ fetchBooks ทุกครั้งที่ตัวแปร page เปลี่ยนแปลง
  useEffect(() => {
    fetchBooks(page);
  }, [page]);

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-6xl mx-auto">
        <h1 className="mb-8 text-3xl font-bold text-gray-800">📚 แคตตาล็อกหนังสือ</h1>

        {loading ? (
          <div className="text-center text-gray-500">กำลังโหลดข้อมูล...</div>
        ) : (
          <>
            {/* วาดการ์ดหนังสือ */}
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {books.map((book) => (
                <div key={book.id} className="p-5 transition bg-white border rounded-lg shadow-sm hover:shadow-md">
                  <h2 className="mb-2 text-lg font-bold text-blue-700 truncate">{book.title}</h2>
                  <p className="text-sm text-gray-600">โดย: {book.authors?.join(', ')}</p>
                  <p className="text-sm text-gray-600">หมวดหมู่: {book.categoryName}</p>
                  <div className="mt-4">
                    <span className={`px-2 py-1 text-xs font-semibold rounded-full ${book.availableCopies > 0 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {book.availableCopies > 0 ? `ว่าง ${book.availableCopies} เล่ม` : 'ถูกยืมหมดแล้ว'}
                    </span>
                  </div>
                </div>
              ))}
            </div>

            {/* ระบบแบ่งหน้า (Pagination) */}
            <div className="flex items-center justify-center mt-10 space-x-4">
              <button 
                onClick={() => setPage(page - 1)} 
                disabled={page === 0}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border rounded-md disabled:opacity-50 hover:bg-gray-50"
              >
                ก่อนหน้า
              </button>
              <span className="text-sm text-gray-600">
                หน้า {page + 1} จาก {totalPages === 0 ? 1 : totalPages}
              </span>
              <button 
                onClick={() => setPage(page + 1)} 
                disabled={page >= totalPages - 1}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border rounded-md disabled:opacity-50 hover:bg-gray-50"
              >
                ถัดไป
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}