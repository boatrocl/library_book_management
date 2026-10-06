import { useState, useEffect } from 'react';
import api from '../api';

export default function Catalog() {
  const [books, setBooks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  
  // เพิ่ม State สำหรับระบบค้นหา
  const [searchInput, setSearchInput] = useState('');
  const [keyword, setKeyword] = useState('');

  const fetchBooks = async (currentPage, currentKeyword) => {
    setLoading(true);
    try {
      // ประกอบ URL โดยเช็กว่ามีคำค้นหาหรือไม่ (ป้องกันการส่ง keyword ว่างเปล่าไปกวน API)
      const keywordParam = currentKeyword ? `&keyword=${encodeURIComponent(currentKeyword)}` : '';
      const response = await api.get(`/api/v1/books?page=${currentPage}&size=8${keywordParam}`);
      
      setBooks(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("เกิดข้อผิดพลาดในการดึงข้อมูลหนังสือ", error);
    } finally {
      setLoading(false);
    }
  };

  // ดึงข้อมูลเมื่อ page หรือ keyword เปลี่ยน
  useEffect(() => {
    fetchBooks(page, keyword);
  }, [page, keyword]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0); // รีเซ็ตกลับไปหน้าแรกเสมอเมื่อค้นหาใหม่
    setKeyword(searchInput);
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setKeyword('');
    setPage(0);
  };

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-6xl mx-auto">
        
        {/* ส่วนหัวและระบบค้นหา */}
        <div className="flex flex-col items-center justify-between mb-8 md:flex-row">
          <h1 className="mb-4 text-3xl font-bold text-gray-800 md:mb-0">📚 แคตตาล็อกหนังสือ</h1>
          
          <form onSubmit={handleSearch} className="flex w-full md:w-auto">
            <input
              type="text"
              placeholder="ค้นหาชื่อหนังสือ..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="w-full px-4 py-2 border rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500 md:w-64"
            />
            {keyword && (
              <button 
                type="button"
                onClick={handleClearSearch}
                className="px-3 py-2 text-gray-600 bg-gray-100 border-t border-b hover:bg-gray-200"
              >
                ✕
              </button>
            )}
            <button 
              type="submit"
              className="px-4 py-2 font-bold text-white transition bg-blue-600 rounded-r-md hover:bg-blue-700"
            >
              ค้นหา
            </button>
          </form>
        </div>

        {loading ? (
          <div className="py-20 text-xl font-bold text-center text-gray-500">กำลังโหลดข้อมูล...</div>
        ) : (
          <>
            {/* กรณีไม่พบหนังสือ */}
            {books.length === 0 ? (
              <div className="py-20 text-center text-gray-500 bg-white border rounded-lg shadow-sm">
                ไม่พบหนังสือที่ค้นหา
              </div>
            ) : (
              <>
                {/* วาดการ์ดหนังสือ */}
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
                  {books.map((book) => (
                    <div key={book.id} className="flex flex-col justify-between p-5 transition bg-white border rounded-lg shadow-sm hover:shadow-md">
                      <div>
                        <h2 className="mb-2 text-lg font-bold text-blue-700 line-clamp-2" title={book.title}>
                          {book.title}
                        </h2>
                        <p className="text-sm text-gray-600 line-clamp-1">โดย: {book.authors?.join(', ')}</p>
                        <p className="text-sm text-gray-600">หมวดหมู่: {book.categoryName}</p>
                      </div>
                      <div className="mt-4">
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${book.availableCopies > 0 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                          {book.availableCopies > 0 ? `ว่าง ${book.availableCopies} เล่ม` : 'ถูกยืมหมดแล้ว'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                {/* ระบบแบ่งหน้า (Pagination) */}
                {totalPages > 1 && (
                  <div className="flex items-center justify-center mt-10 space-x-4">
                    <button 
                      onClick={() => setPage(page - 1)} 
                      disabled={page === 0}
                      className="px-4 py-2 text-sm font-medium text-gray-700 transition bg-white border rounded-md disabled:opacity-50 hover:bg-gray-50"
                    >
                      ก่อนหน้า
                    </button>
                    <span className="text-sm font-medium text-gray-600">
                      หน้า {page + 1} จาก {totalPages}
                    </span>
                    <button 
                      onClick={() => setPage(page + 1)} 
                      disabled={page >= totalPages - 1}
                      className="px-4 py-2 text-sm font-medium text-gray-700 transition bg-white border rounded-md disabled:opacity-50 hover:bg-gray-50"
                    >
                      ถัดไป
                    </button>
                  </div>
                )}
              </>
            )}
          </>
        )}
      </div>
    </div>
  );
}