import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

export default function BookManagement() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const navigate = useNavigate();
  
  // State สำหรับควบคุม Modal และฟอร์ม
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    isbn: '',
    title: '',
    publishYear: '',
    price: '',
    categoryId: '',
    publisherId: '',
    authorIds: '' // รับเป็น string เช่น "1, 2" แล้วแปลงเป็น array ตอนส่ง
  });

  const fetchBooks = async () => {
    setLoading(true);
    try {
      // ดึงข้อมูลหนังสือทั้งหมด (สเปครองรับ Pagination แต่เพื่อความง่ายในหน้าจัดการเราจะดึงหน้าแรกมาแสดงก่อน)
      const response = await api.get('/api/v1/books?size=50');
      setBooks(response.data.content || []);
    } catch (error) {
      console.error("Fetch books error:", error);
      setMessage({ type: 'error', text: 'ไม่สามารถดึงข้อมูลหนังสือได้' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBooks();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const openModal = (book = null) => {
    setMessage({ type: '', text: '' });
    if (book) {
      setEditingId(book.id);
      setFormData({
        isbn: book.isbn || '',
        title: book.title || '',
        publishYear: book.publishYear || '',
        price: book.price || '',
        categoryId: book.categoryId || '', // อาจจะต้องดึง ID มาถ้าใน response เดิมไม่มี
        publisherId: book.publisherId || '',
        authorIds: book.authors ? book.authors.join(', ') : '' // แปลงกลับเป็น string
      });
    } else {
      setEditingId(null);
      setFormData({ isbn: '', title: '', publishYear: '', price: '', categoryId: '', publisherId: '', authorIds: '' });
    }
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });

    // จัดเตรียม Payload ให้ตรงกับ CreateBookRequest / UpdateBookRequest
    const payload = {
      isbn: formData.isbn,
      title: formData.title,
      publishYear: parseInt(formData.publishYear),
      price: parseFloat(formData.price),
      categoryId: parseInt(formData.categoryId),
      publisherId: parseInt(formData.publisherId),
      authorIds: formData.authorIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
    };

    try {
      if (editingId) {
        await api.put(`/api/v1/books/${editingId}`, payload);
        setMessage({ type: 'success', text: 'อัปเดตข้อมูลหนังสือสำเร็จ' });
      } else {
        await api.post('/api/v1/books', payload);
        setMessage({ type: 'success', text: 'เพิ่มหนังสือใหม่สำเร็จ' });
      }
      setIsModalOpen(false);
      fetchBooks(); // รีเฟรชตาราง
    } catch (error) {
      console.error("Submit error:", error);
      setMessage({ 
        type: 'error', 
        text: error.response?.data?.message || 'เกิดข้อผิดพลาด ตรวจสอบข้อมูลให้ถูกต้องตามเงื่อนไข (เช่น ISBN ซ้ำ)' 
      });
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('คุณแน่ใจหรือไม่ที่จะลบหนังสือเล่มนี้?')) return;
    
    try {
      await api.delete(`/api/v1/books/${id}`);
      setMessage({ type: 'success', text: 'ลบหนังสือสำเร็จ' });
      fetchBooks();
    } catch (error) {
      console.error("Delete error:", error);
      setMessage({ 
        type: 'error', 
        text: error.response?.status === 409 
          ? 'ลบไม่ได้: ยังมีตัวเล่มถูกยืมหรือจองอยู่ (BR-11)' 
          : 'เกิดข้อผิดพลาดในการลบ' 
      });
    }
  };

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-6xl mx-auto space-y-6">
        
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-800">จัดการหนังสือ</h1>
          <button 
            onClick={() => openModal()}
            className="px-4 py-2 font-bold text-white transition bg-green-600 rounded-md hover:bg-green-700"
          >
            + เพิ่มหนังสือใหม่
          </button>
        </div>

        {message.text && (
          <div className={`p-4 rounded-md ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {message.text}
          </div>
        )}

        {/* ตารางแสดงหนังสือ */}
        <div className="overflow-hidden bg-white border shadow-sm rounded-xl">
          {loading ? (
            <div className="py-10 text-center text-gray-500">กำลังโหลดข้อมูล...</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="p-4 text-sm font-semibold text-gray-700">ISBN</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">ชื่อหนังสือ</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">ปีที่พิมพ์</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">หมวดหมู่</th>
                    <th className="p-4 text-sm font-semibold text-gray-700 text-right">จัดการ</th>
                  </tr>
                </thead>
                <tbody>
                  {books.map(book => (
                    <tr key={book.id} className="border-b hover:bg-gray-50">
                      <td className="p-4 text-sm text-gray-600">{book.isbn}</td>
                      <td className="p-4 text-sm font-medium text-gray-800">{book.title}</td>
                      <td className="p-4 text-sm text-gray-600">{book.publishYear}</td>
                      <td className="p-4 text-sm text-gray-600">{book.categoryName || book.categoryId}</td>
                      <td className="p-4 text-sm text-right space-x-2">
                        <button 
                        onClick={() => navigate(`/admin/books/${book.id}/copies`)}
                        className="px-3 py-1 mr-2 text-xs font-semibold text-green-700 bg-green-100 rounded-md hover:bg-green-200"
                        >
                        จัดการเล่ม
                        </button>
                        <button 
                          onClick={() => openModal(book)}
                          className="px-3 py-1 text-xs font-semibold text-blue-700 bg-blue-100 rounded-md hover:bg-blue-200"
                        >
                          แก้ไข
                        </button>
                        <button 
                          onClick={() => handleDelete(book.id)}
                          className="px-3 py-1 text-xs font-semibold text-red-700 bg-red-100 rounded-md hover:bg-red-200"
                        >
                          ลบ
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Modal สำหรับฟอร์มเพิ่ม/แก้ไขหนังสือ */}
        {isModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50">
            <div className="w-full max-w-2xl bg-white rounded-xl shadow-lg">
              <div className="flex items-center justify-between p-6 border-b">
                <h2 className="text-xl font-bold text-gray-800">{editingId ? 'แก้ไขหนังสือ' : 'เพิ่มหนังสือใหม่'}</h2>
                <button onClick={() => setIsModalOpen(false)} className="text-gray-500 hover:text-gray-700">✕</button>
              </div>
              <div className="p-6">
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">ISBN (10 หรือ 13 หลัก)</label>
                      <input type="text" name="isbn" value={formData.isbn} onChange={handleInputChange} required pattern="[0-9]{10}|[0-9]{13}" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">ชื่อหนังสือ</label>
                      <input type="text" name="title" value={formData.title} onChange={handleInputChange} required maxLength="200" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">ปีที่พิมพ์ (1000 - 2100)</label>
                      <input type="number" name="publishYear" value={formData.publishYear} onChange={handleInputChange} required min="1000" max="2100" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">ราคา</label>
                      <input type="number" name="price" value={formData.price} onChange={handleInputChange} required min="0" step="0.01" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">รหัสหมวดหมู่ (Category ID)</label>
                      <input type="number" name="categoryId" value={formData.categoryId} onChange={handleInputChange} required className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">รหัสสำนักพิมพ์ (Publisher ID)</label>
                      <input type="number" name="publisherId" value={formData.publisherId} onChange={handleInputChange} required className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                  </div>
                  <div>
                    <label className="block mb-1 text-sm font-medium text-gray-700">รหัสผู้แต่ง (Author IDs - คั่นด้วยลูกน้ำ เช่น 1, 2)</label>
                    <input type="text" name="authorIds" value={formData.authorIds} onChange={handleInputChange} required className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" placeholder="เช่น: 1, 3, 5" />
                  </div>
                  <div className="flex justify-end pt-4 space-x-3 border-t">
                    <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200">ยกเลิก</button>
                    <button type="submit" className="px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">บันทึกข้อมูล</button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}