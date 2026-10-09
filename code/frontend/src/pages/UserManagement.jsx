import { useCallback, useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContextValue';
import api from '../api';


export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });

  const { user: currentUser } = useContext(AuthContext);

  const fetchUsers = useCallback(async (signal) => {
    try {
      // ดึงข้อมูลผู้ใช้ทั้งหมด (อาจปรับเปลี่ยน endpoint ตาม API ของจริงที่มี)
      const response = await api.get('/api/v1/users', { signal });
      if (signal?.aborted) return;
      setUsers(response.data.content || response.data || []);
    } catch (error) {
      if (signal?.aborted) return;
      console.error("Fetch users error:", error);
      setMessage({ type: 'error', text: 'ไม่สามารถดึงข้อมูลผู้ใช้งานได้' });
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
    if (!window.confirm(`ยืนยันการเปลี่ยนสถานะผู้ใช้เป็น ${newStatus}?`)) return;
    
    try {
      // ส่งคำขอเปลี่ยนสถานะ
      await api.patch(`/api/v1/users/${id}/status`, { status: newStatus });
      setMessage({ type: 'success', text: `อัปเดตสถานะผู้ใช้รหัส ${id} สำเร็จ` });
      setLoading(true);
      await fetchUsers();
    } catch (error) {
      console.error("Update status error:", error);
      setMessage({ type: 'error', text: 'เกิดข้อผิดพลาดในการเปลี่ยนสถานะ' });
    }
  };

  const handleRoleChange = async (id, newRole) => {
    if (!window.confirm(`ยืนยันการปรับสิทธิ์ผู้ใช้เป็น ${newRole}?`)) return;
    
    try {
      // ส่งคำขอเปลี่ยน Role
      await api.patch(`/api/v1/users/${id}/role`, { role: newRole });
      setMessage({ type: 'success', text: `ปรับสิทธิ์ผู้ใช้รหัส ${id} สำเร็จ` });
      setLoading(true);
      await fetchUsers();
    } catch (error) {
      console.error("Update role error:", error);
      setMessage({ type: 'error', text: 'เกิดข้อผิดพลาดในการปรับสิทธิ์' });
    }
  };

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-6xl mx-auto space-y-6">
        <h1 className="text-3xl font-bold text-gray-800">จัดการผู้ใช้งาน</h1>

        {message.text && (
          <div className={`p-4 rounded-md font-medium ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {message.text}
          </div>
        )}

        <div className="overflow-hidden bg-white border shadow-xs rounded-xl">
          <div className="p-4 border-b bg-gray-50">
            <h2 className="text-lg font-bold text-gray-800">รายชื่อผู้ใช้งานในระบบ</h2>
          </div>
          
          {loading ? (
            <div className="py-10 text-center text-gray-500">กำลังโหลดข้อมูล...</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="p-4 text-sm font-semibold text-gray-700">ID</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">Username (อีเมล)</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">ระดับ (Tier)</th>
                    <th className="p-4 text-sm font-semibold text-gray-700">สถานะบัญชี</th>
                    <th className="p-4 text-sm font-semibold text-right text-gray-700">สิทธิ์ / จัดการ</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.id} className="border-b hover:bg-gray-50">
                      <td className="p-4 text-sm text-gray-600">{u.id}</td>
                      <td className="p-4 text-sm font-medium text-gray-800">
                        {u.username} <br/><span className="text-xs text-gray-500 font-normal">{u.email}</span>
                      </td>
                      <td className="p-4 text-sm text-gray-600">{u.memberTier || '-'}</td>
                      <td className="p-4 text-sm">
                        <button
                          onClick={() => handleStatusChange(u.id, u.isActive ? 'ACTIVE' : 'SUSPENDED')}
                          className={`px-3 py-1 text-xs font-bold rounded-full transition ${
                            u.isActive 
                              ? 'bg-green-100 text-green-800 hover:bg-green-200' 
                              : 'bg-red-100 text-red-800 hover:bg-red-200'
                          }`}
                        >
                          {u.isActive ? 'ACTIVE' : 'SUSPENDED'}
                        </button>
                      </td>
                      <td className="p-4 text-sm text-right space-x-2 flex justify-end items-center">
                        <select
                          value={u.role}
                          disabled={u.username === currentUser?.username} // ป้องกันแก้ role ของบัญชีที่ล็อกอินอยู่
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          className="px-2 py-1 text-sm border rounded-md focus:ring-blue-500 bg-gray-50"
                        >
                          <option value="MEMBER">MEMBER</option>
                          <option value="LIBRARIAN">LIBRARIAN</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                  {users.length === 0 && (
                    <tr><td colSpan="5" className="p-6 text-center text-gray-500">ไม่พบข้อมูลผู้ใช้งาน</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
