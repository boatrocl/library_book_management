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
                        {loan.items && loan.items.length > 0 
                          ? loan.items.map(item => item.bookTitle).join(', ') 
                          : '-'}
                      </td>
                      <td className="p-3 text-sm text-gray-600">
                        {loan.loanDate ? new Date(loan.loanDate).toLocaleDateString(language === 'th' ? 'th-TH' : 'en-US') : '-'}
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
