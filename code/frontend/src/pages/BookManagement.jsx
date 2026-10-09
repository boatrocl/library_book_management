import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { useLanguage } from '../context/LanguageContext';

export default function BookManagement() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', th: '', en: '' });
  const { t, categoryName } = useLanguage();

  const navigate = useNavigate();
  
  // State สำหรับควบคุม Modal และฟอร์ม
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formError, setFormError] = useState('');
  const [formFieldErrors, setFormFieldErrors] = useState([]);
  const [formData, setFormData] = useState({
    isbn: '',
    title: '',
    publishYear: '',
    price: '',
    categoryId: '',
    publisherId: '',
    authorIds: '' // รับเป็น string เช่น "1, 2" แล้วแปลงเป็น array ตอนส่ง
  });

  const fetchBooks = useCallback(async (signal) => {
    try {
      // ดึงข้อมูลหนังสือทั้งหมด (สเปครองรับ Pagination แต่เพื่อความง่ายในหน้าจัดการเราจะดึงหน้าแรกมาแสดงก่อน)
      const response = await api.get('/api/v1/books?size=50', { signal });
      if (signal?.aborted) return;
      setBooks(response.data.content || []);
    } catch (error) {
      if (signal?.aborted) return;
      console.error("Fetch books error:", error);
      setMessage({ type: 'error', th: 'ไม่สามารถดึงข้อมูลหนังสือได้', en: 'Could not load books.' });
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void Promise.resolve().then(() => {
      if (!controller.signal.aborted) return fetchBooks(controller.signal);
    });
    return () => controller.abort();
  }, [fetchBooks]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormError('');
    setFormFieldErrors([]);
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const openModal = (book = null) => {
    setMessage({ type: '', th: '', en: '' });
    setFormError('');
    setFormFieldErrors([]);
    if (book) {
      setEditingId(book.id);
      setFormData({
        isbn: book.isbn || '',
        title: book.title || '',
        publishYear: book.publishYear || '',
        price: book.price || '',
        categoryId: book.categoryId ?? '',
        publisherId: book.publisherId ?? '',
        authorIds: Array.isArray(book.authorIds) ? book.authorIds.join(', ') : ''
      });
    } else {
      setEditingId(null);
      setFormData({ isbn: '', title: '', publishYear: '', price: '', categoryId: '', publisherId: '', authorIds: '' });
    }
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', th: '', en: '' });
    setFormError('');
    setFormFieldErrors([]);

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
        setMessage({ type: 'success', th: 'อัปเดตข้อมูลหนังสือสำเร็จ', en: 'Book updated successfully.' });
      } else {
        await api.post('/api/v1/books', payload);
        setMessage({ type: 'success', th: 'เพิ่มหนังสือใหม่สำเร็จ', en: 'Book added successfully.' });
      }
      setIsModalOpen(false);
      setLoading(true);
      await fetchBooks(); // รีเฟรชตาราง
    } catch (error) {
      console.error("Submit error:", error);
      setFormFieldErrors(Array.isArray(error.response?.data?.fieldErrors) ? error.response.data.fieldErrors : []);
      setFormError(error.response?.data?.message || t(
        'เกิดข้อผิดพลาด ตรวจสอบข้อมูลให้ถูกต้องตามเงื่อนไข (เช่น ISBN ซ้ำ)',
        'Could not save the book. Check the details and make sure the ISBN is not already in use.'
      ));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('คุณแน่ใจหรือไม่ที่จะลบหนังสือเล่มนี้?', 'Are you sure you want to delete this book?'))) return;
    
    try {
      await api.delete(`/api/v1/books/${id}`);
      setMessage({ type: 'success', th: 'ลบหนังสือสำเร็จ', en: 'Book deleted successfully.' });
      setLoading(true);
      await fetchBooks();
    } catch (error) {
      console.error("Delete error:", error);
      setMessage({
        type: 'error',
        th: error.response?.status === 409 ? 'ลบไม่ได้: ยังมีตัวเล่มถูกยืมหรือจองอยู่ (BR-11)' : 'เกิดข้อผิดพลาดในการลบ',
        en: error.response?.status === 409 ? 'Cannot delete this book while copies are on loan or reserved (BR-11).' : 'Could not delete the book.',
      });
    }
  };

  return (
    <main className="lf-workspace-page">
      <div className="lf-workspace-content max-w-6xl mx-auto">
        
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-800">{t('จัดการหนังสือ', 'Manage books')}</h1>
          <button 
            onClick={() => openModal()}
            className="px-4 py-2 font-bold text-white transition bg-green-600 rounded-md hover:bg-green-700"
          >
            + {t('เพิ่มหนังสือใหม่', 'Add a book')}
          </button>
        </div>

        {(message.th || message.en) && (
          <div className={`p-4 rounded-md ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {t(message.th, message.en)}
          </div>
        )}

        {/* ตารางแสดงหนังสือ */}
        <div className="overflow-hidden bg-white border shadow-xs rounded-xl">
          {loading ? (
            <div className="py-10 text-center text-gray-500">{t('กำลังโหลดข้อมูล...', 'Loading books...')}</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="p-4 text-sm font-semibold text-gray-700">ISBN</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('ชื่อหนังสือ', 'Title')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('ปีที่พิมพ์', 'Published')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('หมวดหมู่', 'Category')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700 text-right">{t('จัดการ', 'Actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {books.map(book => (
                    <tr key={book.id} className="border-b hover:bg-gray-50">
                      <td className="p-4 text-sm text-gray-600">{book.isbn}</td>
                      <td className="p-4 text-sm font-medium text-gray-800">{book.title}</td>
                      <td className="p-4 text-sm text-gray-600">{book.publishYear}</td>
                      <td className="p-4 text-sm text-gray-600">{categoryName(book.categoryName) || book.categoryId}</td>
                      <td className="p-4 text-sm text-right space-x-2">
                        <button 
                        onClick={() => navigate(`/admin/books/${book.id}/copies`)}
                        className="px-3 py-1 mr-2 text-xs font-semibold text-green-700 bg-green-100 rounded-md hover:bg-green-200"
                        >
                        {t('จัดการเล่ม', 'Manage copies')}
                        </button>
                        <button 
                          onClick={() => openModal(book)}
                          className="px-3 py-1 text-xs font-semibold text-blue-700 bg-blue-100 rounded-md hover:bg-blue-200"
                        >
                          {t('แก้ไข', 'Edit')}
                        </button>
                        <button 
                          onClick={() => handleDelete(book.id)}
                          className="px-3 py-1 text-xs font-semibold text-red-700 bg-red-100 rounded-md hover:bg-red-200"
                        >
                          {t('ลบ', 'Delete')}
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
          <div className="book-management-modal-backdrop fixed inset-0 z-50 flex items-center justify-center overflow-y-auto p-4">
            <div className="book-management-modal w-full max-w-2xl bg-white rounded-xl shadow-lg" role="dialog" aria-modal="true" aria-labelledby="book-management-modal-title">
              <div className="flex items-center justify-between p-6 border-b">
                <h2 id="book-management-modal-title" className="text-xl font-bold text-gray-800">{editingId ? t('แก้ไขหนังสือ', 'Edit book') : t('เพิ่มหนังสือใหม่', 'Add a book')}</h2>
                <button onClick={() => { setIsModalOpen(false); setFormError(''); setFormFieldErrors([]); }} className="text-gray-500 hover:text-gray-700" aria-label={t('ปิด', 'Close')}>✕</button>
              </div>
              <div className="p-6">
                <form onSubmit={handleSubmit} className="space-y-4">
                  {formError && (
                    <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800" role="alert" aria-live="assertive">
                      {formError}
                      {formFieldErrors.length > 0 && (
                        <ul className="mt-2 list-inside list-disc">
                          {formFieldErrors.map((fieldError, index) => (
                            <li key={`${fieldError.field}-${index}`}>{fieldError.message}</li>
                          ))}
                        </ul>
                      )}
                    </div>
                  )}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">{t('ISBN (10 หรือ 13 หลัก)', 'ISBN (10 or 13 digits)')}</label>
                      <input type="text" name="isbn" value={formData.isbn} onChange={handleInputChange} required pattern="[0-9]{10}|[0-9]{13}" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">{t('ชื่อหนังสือ', 'Title')}</label>
                      <input type="text" name="title" value={formData.title} onChange={handleInputChange} required maxLength="200" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">{t('ปีที่พิมพ์ (1000 - 2100)', 'Publication year (1000–2100)')}</label>
                      <input type="number" name="publishYear" value={formData.publishYear} onChange={handleInputChange} required min="1000" max="2100" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">{t('ราคา', 'Price')}</label>
                      <input type="number" name="price" value={formData.price} onChange={handleInputChange} required min="0" step="0.01" className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">{t('รหัสหมวดหมู่ (Category ID)', 'Category ID')}</label>
                      <input type="number" name="categoryId" value={formData.categoryId} onChange={handleInputChange} required className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                      <p className="mt-1 text-xs text-gray-500">{t('ID นี้อ้างอิงหมวดหมู่ที่หนังสือสังกัด ใช้รหัสหมวดหมู่ที่มีอยู่ในระบบ', 'This ID points to the book’s category. Use an existing category ID.')}</p>
                    </div>
                    <div>
                      <label className="block mb-1 text-sm font-medium text-gray-700">{t('รหัสสำนักพิมพ์ (Publisher ID)', 'Publisher ID')}</label>
                      <input type="number" name="publisherId" value={formData.publisherId} onChange={handleInputChange} required className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" />
                      <p className="mt-1 text-xs text-gray-500">{t('ID นี้อ้างอิงสำนักพิมพ์ ใช้รหัสของสำนักพิมพ์ที่มีอยู่ในระบบ', 'This ID points to the publisher. Use an existing publisher ID.')}</p>
                    </div>
                  </div>
                  <div>
                    <label className="block mb-1 text-sm font-medium text-gray-700">{t('รหัสผู้แต่ง (Author IDs - คั่นด้วยลูกน้ำ เช่น 1, 2)', 'Author IDs (comma-separated, e.g. 1, 2)')}</label>
                    <input type="text" name="authorIds" value={formData.authorIds} onChange={handleInputChange} required className="w-full px-3 py-2 border rounded-md focus:ring-blue-500 focus:border-blue-500" placeholder="1, 3, 5" />
                    <p className="mt-1 text-xs text-gray-500">{t('กรอกรหัสผู้แต่ง ไม่ใช่ชื่อผู้แต่ง หากมีหลายคนให้คั่นแต่ละรหัสด้วยลูกน้ำ', 'Enter author IDs, not names. Separate multiple IDs with commas.')}</p>
                  </div>
                  <div className="flex justify-end pt-4 space-x-3 border-t">
                    <button type="button" onClick={() => { setIsModalOpen(false); setFormError(''); setFormFieldErrors([]); }} className="px-4 py-2 font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200">{t('ยกเลิก', 'Cancel')}</button>
                    <button type="submit" className="px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">{t('บันทึกข้อมูล', 'Save')}</button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

      </div>
    </main>
  );
}
