import { useCallback, useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContextValue';
import { useLanguage } from '../context/LanguageContext';
import api from '../api';


export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', th: '', en: '' });

  const { user: currentUser } = useContext(AuthContext);
  const { t, enumLabel } = useLanguage();

  const fetchUsers = useCallback(async (signal) => {
    try {
      // ดึงข้อมูลผู้ใช้ทั้งหมด (อาจปรับเปลี่ยน endpoint ตาม API ของจริงที่มี)
      const response = await api.get('/api/v1/users', { signal });
      if (signal?.aborted) return;
      setUsers(response.data.content || response.data || []);
    } catch (error) {
      if (signal?.aborted) return;
      console.error("Fetch users error:", error);
      setMessage({ type: 'error', th: 'ไม่สามารถดึงข้อมูลผู้ใช้งานได้', en: 'Could not load users.' });
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void Promise.resolve().then(() => {
      if (!controller.signal.aborted) return fetchUsers(controller.signal);
    });
    return () => controller.abort();
  }, [fetchUsers]);

  const handleStatusChange = async (id, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    if (!window.confirm(t(`ยืนยันการเปลี่ยนสถานะผู้ใช้เป็น ${enumLabel(newStatus)}?`, `Change this user's status to ${newStatus}?`))) return;
    
    try {
      // ส่งคำขอเปลี่ยนสถานะ
      await api.patch(`/api/v1/users/${id}/status`, { status: newStatus });
      setMessage({ type: 'success', th: `อัปเดตสถานะผู้ใช้รหัส ${id} สำเร็จ`, en: `Status for user ${id} updated successfully.` });
      setLoading(true);
      await fetchUsers();
    } catch (error) {
      console.error("Update status error:", error);
      setMessage({ type: 'error', th: 'เกิดข้อผิดพลาดในการเปลี่ยนสถานะ', en: 'Could not update the user status.' });
    }
  };

  const handleRoleChange = async (id, newRole) => {
    if (!window.confirm(t(`ยืนยันการปรับสิทธิ์ผู้ใช้เป็น ${enumLabel(newRole)}?`, `Change this user's role to ${newRole}?`))) return;
    
    try {
      // ส่งคำขอเปลี่ยน Role
      await api.patch(`/api/v1/users/${id}/role`, { role: newRole });
      setMessage({ type: 'success', th: `ปรับสิทธิ์ผู้ใช้รหัส ${id} สำเร็จ`, en: `Role for user ${id} updated successfully.` });
      setLoading(true);
      await fetchUsers();
    } catch (error) {
      console.error("Update role error:", error);
      setMessage({ type: 'error', th: 'เกิดข้อผิดพลาดในการปรับสิทธิ์', en: 'Could not update the user role.' });
    }
  };

  return (
    <main className="lf-workspace-page">
      <div className="lf-workspace-content max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-800">{t('จัดการผู้ใช้งาน', 'Manage users')}</h1>

        {(message.th || message.en) && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {t(message.th, message.en)}
          </div>
        )}

        <div className="overflow-hidden bg-white border shadow-xs rounded-xl">
          <div className="p-4 border-b bg-gray-50">
            <h2 className="text-lg font-bold text-gray-800">{t('รายชื่อผู้ใช้งานในระบบ', 'Registered users')}</h2>
          </div>
          
          {loading ? (
            <div className="py-10 text-center text-gray-500">{t('กำลังโหลดข้อมูล...', 'Loading users...')}</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="p-4 text-sm font-semibold text-gray-700">ID</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('ผู้ใช้ (อีเมล)', 'Username (email)')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('ระดับสมาชิก', 'Tier')}</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">{t('สถานะบัญชี', 'Account status')}</th>
                    <th className="p-4 text-sm font-semibold text-right text-gray-700">{t('สิทธิ์ / จัดการ', 'Role / actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.id} className="border-b hover:bg-gray-50">
                      <td className="p-4 text-sm text-gray-600">{u.id}</td>
                      <td className="p-4 text-sm font-medium text-gray-800">
                        {u.username} <br/><span className="text-xs text-gray-500 font-normal">{u.email}</span>
                      </td>
                      <td className="p-4 text-sm text-gray-600">{enumLabel(u.memberTier) || '-'}</td>
                      <td className="p-4 text-sm">
                        <button
                          onClick={() => handleStatusChange(u.id, u.isActive ? 'ACTIVE' : 'SUSPENDED')}
                          className={`px-3 py-1 text-xs font-bold rounded-full transition ${
                            u.isActive 
                              ? 'bg-green-100 text-green-800 hover:bg-green-200' 
                              : 'bg-red-100 text-red-800 hover:bg-red-200'
                          }`}
                        >
                          {enumLabel(u.isActive ? 'ACTIVE' : 'SUSPENDED')}
                        </button>
                      </td>
                      <td className="p-4 text-sm text-right space-x-2 flex justify-end items-center">
                        <select
                          value={u.role}
                          disabled={u.username === currentUser?.username}
                          aria-label={t(`เปลี่ยนสิทธิ์ของ ${u.username}`, `Change role for ${u.username}`)}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          className="px-2 py-1 text-sm border rounded-md focus:ring-blue-500 bg-gray-50"
                        >
                          <option value="MEMBER">{enumLabel('MEMBER')}</option>
                          <option value="LIBRARIAN">{enumLabel('LIBRARIAN')}</option>
                          <option value="ADMIN">{enumLabel('ADMIN')}</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                  {users.length === 0 && (
                    <tr><td colSpan="5" className="p-6 text-center text-gray-500">{t('ไม่พบข้อมูลผู้ใช้งาน', 'No users found.')}</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
