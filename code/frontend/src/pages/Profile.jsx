import { useCallback, useContext, useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../api';
import { AuthContext } from '../context/AuthContextValue';
import { useLanguage } from '../context/LanguageContext';

function readSession() {
  const token = localStorage.getItem('token');
  if (!token) return { userId: null, errorKey: 'loginRequired' };

  try {
    const decoded = jwtDecode(token);
    if (decoded.exp && decoded.exp * 1000 <= Date.now()) {
      localStorage.removeItem('token');
      return { userId: null, errorKey: 'sessionExpired' };
    }

    const userId = decoded.id || decoded.userId || decoded.sub;
    return userId
      ? { userId, error: '' }
      : { userId: null, errorKey: 'invalidSession' };
  } catch (error) {
    console.error('Token ไม่ถูกต้อง:', error);
    localStorage.removeItem('token');
    return { userId: null, errorKey: 'invalidSession' };
  }
}

function formatDate(value, language) {
  if (!value) return '-';
  const datePart = String(value).slice(0, 10);
  const date = new Date(`${datePart}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(language === 'th' ? 'th-TH' : 'en-US', { dateStyle: 'medium' }).format(date);
}

export default function Profile() {
  const { user } = useContext(AuthContext);
  const { language, t, enumLabel } = useLanguage();
  const isMember = user?.role === 'MEMBER';
  const [profile, setProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [session] = useState(readSession);
  const { userId } = session;
  const [message, setMessage] = useState(() => ({
    type: session.errorKey ? 'error' : '',
    key: session.errorKey || '',
    th: '',
    en: '',
  }));
  const [isLoading, setIsLoading] = useState(Boolean(userId));
  
  // State ใหม่สำหรับเก็บข้อมูลการยืมและค่าปรับ
  const [loans, setLoans] = useState([]);
  const [fines, setFines] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [reservationAction, setReservationAction] = useState({ id: null, message: '' });
  const [selectedLoanDetails, setSelectedLoanDetails] = useState(null);
  const [selectedBook, setSelectedBook] = useState(null);
  const [bookDetailsLoading, setBookDetailsLoading] = useState(false);
  const [bookDetailsError, setBookDetailsError] = useState('');

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    address: ''
  });

  const messageText = (currentMessage) => {
    if (currentMessage.th || currentMessage.en) return t(currentMessage.th, currentMessage.en);
    const sessionMessages = {
      loginRequired: t('กรุณาเข้าสู่ระบบ', 'Please sign in.'),
      sessionExpired: t('เซสชันหมดอายุ กรุณาเข้าสู่ระบบใหม่', 'Your session has expired. Please sign in again.'),
      invalidSession: t('เซสชันไม่ถูกต้อง กรุณาล็อกอินใหม่', 'Your session is invalid. Please sign in again.'),
    };
    return sessionMessages[currentMessage.key] || '';
  };

  const fetchProfileData = useCallback(async (id, signal) => {
    try {
      // Staff can view their profile and loan/fine history, but the self-reservation API is MEMBER-only.
      const reservationsRequest = isMember
        ? api.get('/api/v1/reservations/self?page=0&size=50&sort=reservedAt,desc', { signal })
        : Promise.resolve({ data: { content: [] } });

      const [profileRes, loansRes, finesRes, reservationsRes] = await Promise.all([
        api.get(`/api/v1/members/${id}`, { signal }),
        api.get(`/api/v1/members/${id}/loans`, { signal }),
        api.get(`/api/v1/members/${id}/fines?status=UNPAID`, { signal }),
        reservationsRequest
      ]);
      if (signal?.aborted) return;

      setProfile(profileRes.data);
      setFormData({
        firstName: profileRes.data.firstName || '',
        lastName: profileRes.data.lastName || '',
        phoneNumber: profileRes.data.phoneNumber || '',
        address: profileRes.data.address || ''
      });

      // รองรับกรณีที่ API ส่งกลับมาเป็นอาร์เรย์โดยตรง หรืออยู่ในรูปแบบ PageResponse (.content)
      setLoans(loansRes.data.content || loansRes.data || []);
      setFines(finesRes.data.content || finesRes.data || []);
      setReservations(reservationsRes.data.content || []);
    } catch (error) {
      if (signal?.aborted) return;
      console.error("ดึงข้อมูลล้มเหลว:", error);
      setMessage({ type: 'error', th: `ไม่สามารถดึงข้อมูลโปรไฟล์ได้ (ID: ${id})`, en: `Could not load profile (ID: ${id}).` });
    } finally {
      if (!signal?.aborted) setIsLoading(false);
    }
  }, [isMember]);

  useEffect(() => {
    if (!userId) return undefined;

    const controller = new AbortController();
    void Promise.resolve().then(() => {
      if (!controller.signal.aborted) return fetchProfileData(userId, controller.signal);
    });
    return () => controller.abort();
  }, [fetchProfileData, userId]);

  useEffect(() => {
    const bookId = selectedLoanDetails?.item?.bookId;
    if (!bookId) return undefined;

    const controller = new AbortController();
    api.get(`/api/v1/books/${bookId}`, { signal: controller.signal })
      .then((response) => {
        if (!controller.signal.aborted) setSelectedBook(response.data);
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          console.error('Could not load loan book details:', error);
          setBookDetailsError(t('ไม่สามารถโหลดรายละเอียดหนังสือได้', 'Could not load book details.'));
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setBookDetailsLoading(false);
      });

    return () => controller.abort();
  }, [selectedLoanDetails?.item?.bookId, t]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', th: '', en: '', key: '' });
    
    try {
      const response = await api.put(`/api/v1/members/${userId}`, formData);
      setProfile(response.data); 
      setIsEditing(false);
      setMessage({ type: 'success', th: 'บันทึกข้อมูลเรียบร้อยแล้ว!', en: 'Profile saved successfully.' });
    } catch (error) {
      console.error("อัปเดตข้อมูลล้มเหลว", error);
      setMessage({ type: 'error', th: 'กรุณาตรวจสอบข้อมูลอีกครั้ง', en: 'Please check the information and try again.' });
    }
  };

  const handleCancelReservation = async (reservation) => {
    setReservationAction({ id: reservation.id, message: '' });
    try {
      await api.delete(`/api/v1/reservations/${reservation.id}`);
      setReservations((current) => current.map((item) => item.id === reservation.id
        ? { ...item, status: 'CANCELLED' }
        : item));
      setReservationAction({ id: null, message: t('ยกเลิกรายการจองแล้ว', 'Reservation cancelled.') });
    } catch (error) {
      console.error('ยกเลิกรายการจองไม่สำเร็จ', error);
      setReservationAction({ id: null, message: t('ยกเลิกรายการจองไม่สำเร็จ กรุณาลองอีกครั้ง', 'Could not cancel the reservation. Please try again.') });
    }
  };

  if (isLoading) return <main className="lf-workspace-page"><div className="lf-workspace-content lf-workspace-content--narrow"><div className="lf-loading-card">{t('กำลังโหลดข้อมูล...', 'Loading profile...')}</div></div></main>;

  if (!profile) return (
    <main className="lf-workspace-page">
      <div className="lf-workspace-content lf-workspace-content--narrow">
        <div className="lf-form-message lf-form-message--error" role="alert">{messageText(message)}</div>
      </div>
    </main>
  );

  // คำนวณยอดค่าปรับรวม
  const totalFines = fines.reduce((sum, fine) => sum + (fine.amount || 0), 0);

  return (
    <main className="lf-workspace-page">
      <div className="lf-workspace-content lf-workspace-content--narrow max-w-4xl mx-auto">
        
        {/* กล่อง 1: ข้อมูลโปรไฟล์หลัก (ของเดิม) */}
        <div className="overflow-hidden bg-white border shadow-xs rounded-xl">
          <div className="p-6 text-white bg-blue-700">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold">{profile.username}</h1>
                <p className="text-blue-100">{profile.email}</p>
              </div>
              <div className="text-right">
                {isMember && (
                  <span className="px-3 py-1 text-sm font-bold text-blue-700 bg-white rounded-full">
                    {t('ระดับสมาชิก', 'Tier')}: {enumLabel(profile.memberTier)}
                  </span>
                )}
                <p className="mt-2 text-sm text-blue-200">{t('สิทธิ์', 'Role')}: {enumLabel(profile.role)}</p>
              </div>
            </div>
          </div>

          <div className="p-8">
            {(message.th || message.en || message.key) && (
              <div className={`p-4 mb-6 text-sm rounded-md ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                {messageText(message)}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <div>
                  <label className="block mb-1 text-sm font-medium text-gray-700">{t('ชื่อจริง', 'First name')}</label>
                  <input 
                    type="text" 
                    disabled={!isEditing}
                    value={formData.firstName}
                    onChange={(e) => setFormData({...formData, firstName: e.target.value})}
                    className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                    required 
                  />
                </div>
                <div>
                  <label className="block mb-1 text-sm font-medium text-gray-700">{t('นามสกุล', 'Last name')}</label>
                  <input 
                    type="text" 
                    disabled={!isEditing}
                    value={formData.lastName}
                    onChange={(e) => setFormData({...formData, lastName: e.target.value})}
                    className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                    required 
                  />
                </div>
              </div>

              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">{t('เบอร์โทรศัพท์', 'Phone number')}</label>
                <input 
                  type="text" 
                  disabled={!isEditing}
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})}
                  className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                />
              </div>

              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">{t('ที่อยู่', 'Address')}</label>
                <textarea 
                  disabled={!isEditing}
                  rows="3"
                  value={formData.address}
                  onChange={(e) => setFormData({...formData, address: e.target.value})}
                  className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                ></textarea>
              </div>

              <div className="flex justify-end pt-4 space-x-4 border-t">
                {!isEditing ? (
                  <button 
                    type="button" 
                    onClick={() => setIsEditing(true)}
                    className="px-6 py-2 font-bold text-white transition bg-blue-600 rounded-md hover:bg-blue-700"
                  >
                    {t('แก้ไขข้อมูล', 'Edit profile')}
                  </button>
                ) : (
                  <>
                    <button 
                      type="button" 
                      onClick={() => {
                        setIsEditing(false);
                        setFormData({
                          firstName: profile.firstName || '',
                          lastName: profile.lastName || '',
                          phoneNumber: profile.phoneNumber || '',
                          address: profile.address || ''
                        });
                      }}
                      className="px-6 py-2 font-bold text-gray-700 transition bg-gray-200 rounded-md hover:bg-gray-300"
                    >
                      {t('ยกเลิก', 'Cancel')}
                    </button>
                    <button 
                      type="submit" 
                      className="px-6 py-2 font-bold text-white transition bg-green-600 rounded-md hover:bg-green-700"
                    >
                      {t('บันทึกข้อมูล', 'Save changes')}
                    </button>
                  </>
                )}
              </div>
            </form>
          </div>
        </div>

        {/* กล่อง 2: แจ้งเตือนค่าปรับ (โผล่มาเฉพาะตอนมีค่าปรับค้างชำระ) */}
        {fines.length > 0 && (
          <div className="p-6 bg-white border border-red-200 shadow-xs rounded-xl">
            <h2 className="mb-4 text-xl font-bold text-red-700">{t('ค่าปรับค้างชำระ (รวม:', 'Outstanding fines (total:')} {totalFines.toLocaleString(language === 'th' ? 'th-TH' : 'en-US')} {t('บาท)', 'THB)')}</h2>
            <ul className="space-y-2">
              {fines.map(fine => (
                <li key={fine.id} className="flex justify-between p-3 rounded-md bg-red-50">
                  <span className="text-red-800">{fine.reason || t('ค่าปรับส่งหนังสือล่าช้า', 'Overdue return fine')}</span>
                  <span className="font-bold text-red-700">{fine.amount?.toLocaleString(language === 'th' ? 'th-TH' : 'en-US')} {t('บาท', 'THB')}</span>
                </li>
              ))}
            </ul>
            <p className="mt-4 text-sm text-gray-600">* {t('กรุณาติดต่อบรรณารักษ์เพื่อชำระค่าปรับ การมียอดค้างชำระจะทำให้คุณไม่สามารถยืมหนังสือเพิ่มได้', 'Contact a librarian to pay your fines. Unpaid fines prevent you from borrowing more books.')}</p>
          </div>
        )}

        {/* กล่อง 3: ประวัติการยืม */}
        <div className="p-6 bg-white border shadow-xs rounded-xl">
          <h2 className="mb-4 text-xl font-bold text-gray-800">{t('ประวัติการยืมหนังสือ', 'Loan history')}</h2>
          {loans.length === 0 ? (
            <p className="py-4 text-center text-gray-500">{t('ยังไม่มีประวัติการยืมหนังสือ', 'No loan history yet.')}</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="p-3 text-sm font-semibold text-gray-700">{t('รหัสใบยืม', 'Loan ID')}</th>
                    <th className="p-3 text-sm font-semibold text-gray-700">{t('หนังสือ', 'Books')}</th>
                    <th className="p-3 text-sm font-semibold text-gray-700">{t('วันที่ยืม', 'Loan date')}</th>
                    <th className="p-3 text-sm font-semibold text-gray-700">{t('สถานะ', 'Status')}</th>
                  </tr>
                </thead>
                <tbody>
                  {loans.map(loan => (
                    <tr key={loan.id} className="border-b hover:bg-gray-50">
                      <td className="p-3 text-sm text-gray-800">{loan.loanCode || `LN-${loan.id}`}</td>
                      <td className="p-3 text-sm text-gray-800">
                        {/* ดึงชื่อหนังสือจาก array items มาต่อกันด้วยลูกน้ำ */}
                        {loan.items?.length > 0 ? (
                          <div className="flex flex-col items-start gap-1">
                            {loan.items.map((item) => (
                              <button
                                key={item.id}
                                type="button"
                                className="text-left font-medium text-teal-800 underline decoration-teal-300 underline-offset-2 hover:text-teal-950 focus:outline-none focus:ring-2 focus:ring-teal-600"
                                onClick={() => {
                                  setSelectedBook(null);
                                  setBookDetailsError(item.bookId ? '' : t('ไม่พบรหัสหนังสือสำหรับโหลดรายละเอียด', 'Book ID is unavailable, so details cannot be loaded.'));
                                  setBookDetailsLoading(Boolean(item.bookId));
                                  setSelectedLoanDetails({ loan, item });
                                }}
                              >
                                {item.bookTitle || t('ดูรายละเอียดหนังสือ', 'View book details')}
                              </button>
                            ))}
                          </div>
                        ) : '-'}
                      </td>
                      <td className="p-3 text-sm text-gray-600">
                        {formatDate(loan.loanDate, language)}
                      </td>
                      <td className="p-3 text-sm">
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full 
                          ${loan.status === 'ACTIVE' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'}`}>
                          {enumLabel(loan.status)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {selectedLoanDetails && (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4"
            role="presentation"
            onClick={(event) => {
              if (event.target === event.currentTarget) setSelectedLoanDetails(null);
            }}
          >
            <section
              className="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl sm:p-8"
              role="dialog"
              aria-modal="true"
              aria-labelledby="loan-book-details-title"
            >
              <div className="flex items-start justify-between gap-4 border-b pb-4">
                <div>
                  <p className="text-sm font-semibold uppercase tracking-wide text-teal-700">{t('รายละเอียดหนังสือและรายการยืม', 'Book and loan item details')}</p>
                  <h2 id="loan-book-details-title" className="mt-1 text-2xl font-bold text-slate-900">
                    {selectedBook?.title || selectedLoanDetails.item.bookTitle || t('กำลังโหลดรายละเอียด…', 'Loading details…')}
                  </h2>
                </div>
                <button
                  type="button"
                  className="rounded-full p-2 text-xl leading-none text-slate-600 hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-teal-600"
                  aria-label={t('ปิดรายละเอียด', 'Close details')}
                  onClick={() => setSelectedLoanDetails(null)}
                >
                  ×
                </button>
              </div>

              {bookDetailsLoading && <p className="py-6 text-slate-600" role="status">{t('กำลังโหลดข้อมูลหนังสือ…', 'Loading book information…')}</p>}
              {bookDetailsError && <p className="mt-5 rounded-lg bg-red-50 p-3 text-sm text-red-800" role="alert">{bookDetailsError}</p>}
              {!bookDetailsLoading && selectedBook && (
                <>
                  <h3 className="mb-3 mt-6 text-lg font-semibold text-slate-900">{t('ข้อมูลหนังสือ', 'Book information')}</h3>
                  <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                    {[
                      [t('รหัสหนังสือ', 'Book ID'), selectedBook.id],
                      ['ISBN', selectedBook.isbn],
                      [t('ชื่อหนังสือ', 'Title'), selectedBook.title],
                      [t('ผู้แต่ง', 'Authors'), selectedBook.authors?.join(', ') || '-'],
                      [t('หมวดหมู่', 'Category'), selectedBook.categoryName || '-'],
                      [t('รหัสหมวดหมู่', 'Category ID'), selectedBook.categoryId ?? '-'],
                      [t('สำนักพิมพ์', 'Publisher'), selectedBook.publisherName || '-'],
                      [t('รหัสสำนักพิมพ์', 'Publisher ID'), selectedBook.publisherId ?? '-'],
                      [t('ปีที่พิมพ์', 'Publication year'), selectedBook.publishYear ?? '-'],
                      [t('ราคา', 'Price'), selectedBook.price ?? '-'],
                      [t('จำนวนตัวเล่ม', 'Total copies'), selectedBook.totalCopies ?? '-'],
                      [t('พร้อมให้ยืม', 'Available copies'), selectedBook.availableCopies ?? '-'],
                    ].map(([label, value]) => (
                      <div key={label} className="rounded-lg bg-slate-50 p-3">
                        <dt className="text-xs font-medium text-slate-500">{label}</dt>
                        <dd className="mt-1 break-words font-semibold text-slate-900">{value}</dd>
                      </div>
                    ))}
                  </dl>
                </>
              )}

              <h3 className="mb-3 mt-6 border-t pt-5 text-lg font-semibold text-slate-900">{t('ข้อมูลการยืม', 'Loan item')}</h3>
              <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="rounded-lg bg-amber-50 p-3">
                  <dt className="text-xs font-medium text-slate-500">{t('รหัสใบยืม', 'Loan ID')}</dt>
                  <dd className="mt-1 font-semibold text-slate-900">{selectedLoanDetails.loan.loanCode || `LN-${selectedLoanDetails.loan.id}`}</dd>
                </div>
                <div className="rounded-lg bg-amber-50 p-3">
                  <dt className="text-xs font-medium text-slate-500">{t('บาร์โค้ดตัวเล่ม', 'Copy barcode')}</dt>
                  <dd className="mt-1 font-semibold text-slate-900">{selectedLoanDetails.item.barcode || '-'}</dd>
                </div>
                <div className="rounded-lg bg-amber-50 p-3">
                  <dt className="text-xs font-medium text-slate-500">{t('วันที่ยืม', 'Loan date')}</dt>
                  <dd className="mt-1 font-semibold text-slate-900">{formatDate(selectedLoanDetails.loan.loanDate, language)}</dd>
                </div>
                <div className="rounded-lg bg-amber-50 p-3">
                  <dt className="text-xs font-medium text-slate-500">{t('กำหนดคืน', 'Due date')}</dt>
                  <dd className="mt-1 font-semibold text-slate-900">{formatDate(selectedLoanDetails.item.dueDate, language)}</dd>
                </div>
                <div className="rounded-lg bg-amber-50 p-3">
                  <dt className="text-xs font-medium text-slate-500">{t('วันที่คืนจริง', 'Returned on')}</dt>
                  <dd className="mt-1 font-semibold text-slate-900">{formatDate(selectedLoanDetails.item.returnedAt, language)}</dd>
                </div>
                <div className="rounded-lg bg-amber-50 p-3">
                  <dt className="text-xs font-medium text-slate-500">{t('สถานะใบยืม', 'Loan status')}</dt>
                  <dd className="mt-1 font-semibold text-slate-900">{enumLabel(selectedLoanDetails.loan.status)}</dd>
                </div>
              </dl>
            </section>
          </div>
        )}

        {isMember && (
          <div className="p-6 bg-white border shadow-xs rounded-xl">
            <h2 className="mb-4 text-xl font-bold text-gray-800">{t('รายการจองหนังสือ', 'My reservations')}</h2>
            {reservationAction.message && (
              <p className="mb-4 text-sm text-teal-800" role="status">{reservationAction.message}</p>
            )}
            {reservations.length === 0 ? (
              <p className="py-4 text-center text-gray-500">{t('ยังไม่มีรายการจองหนังสือ', 'No reservations yet.')}</p>
            ) : (
              <div className="space-y-3">
                {reservations.map((reservation) => {
                  const canCancel = reservation.status === 'WAITING' || reservation.status === 'READY';
                  const reservedDate = reservation.reservedAt
                    ? new Date(reservation.reservedAt).toLocaleString(language === 'th' ? 'th-TH' : 'en-US', { dateStyle: 'medium', timeStyle: 'short' })
                    : '-';
                  const expiresDate = reservation.expiresAt
                    ? new Date(reservation.expiresAt).toLocaleString(language === 'th' ? 'th-TH' : 'en-US', { dateStyle: 'medium', timeStyle: 'short' })
                    : null;
                  return (
                    <article key={reservation.id} className="flex flex-wrap items-center justify-between gap-4 rounded-lg border border-gray-200 p-4">
                      <div className="min-w-0">
                        <h3 className="font-semibold text-gray-900">{reservation.bookTitle}</h3>
                        <p className="mt-1 text-sm text-gray-600">
                          {t('จองเมื่อ', 'Reserved')}: {reservedDate}
                          {reservation.status === 'WAITING' && reservation.queuePosition && (
                            <> · {t('ลำดับคิว', 'Queue position')}: {reservation.queuePosition}</>
                          )}
                          {expiresDate && <> · {t('รับหนังสือก่อน', 'Pick up by')}: {expiresDate}</>}
                        </p>
                        {reservation.reservedBarcode && (
                          <p className="mt-1 text-sm text-gray-600">{t('บาร์โค้ด', 'Barcode')}: {reservation.reservedBarcode}</p>
                        )}
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="rounded-full bg-teal-50 px-3 py-1 text-sm font-medium text-teal-800">{enumLabel(reservation.status)}</span>
                        {canCancel && (
                          <button
                            type="button"
                            className="rounded-md border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-wait disabled:opacity-60"
                            onClick={() => handleCancelReservation(reservation)}
                            disabled={reservationAction.id === reservation.id}
                          >
                            {reservationAction.id === reservation.id ? t('กำลังยกเลิก…', 'Cancelling…') : t('ยกเลิก', 'Cancel')}
                          </button>
                        )}
                      </div>
                    </article>
                  );
                })}
              </div>
            )}
          </div>
        )}

      </div>
    </main>
  );
}
