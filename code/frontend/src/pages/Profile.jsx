import { useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../api';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [userId, setUserId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  
  // State ใหม่สำหรับเก็บข้อมูลการยืมและค่าปรับ
  const [loans, setLoans] = useState([]);
  const [fines, setFines] = useState([]);

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    address: ''
  });

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decoded = jwtDecode(token);
        const currentUserId = decoded.id || decoded.userId || decoded.sub;
        setUserId(currentUserId);
        
        if (!currentUserId) setIsLoading(false);
      } catch (error) {
        console.error("Token ไม่ถูกต้อง:", error);
        setMessage({ type: 'error', text: 'เซสชันไม่ถูกต้อง กรุณาล็อกอินใหม่' });
        setIsLoading(false);
      }
    } else {
      setMessage({ type: 'error', text: 'กรุณาเข้าสู่ระบบ' });
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (userId) {
      fetchProfileData(userId);
    }
  }, [userId]);

  const fetchProfileData = async (id) => {
    setIsLoading(true);
    try {
      // ดึงข้อมูล 3 ส่วนพร้อมกันด้วย Promise.all เพื่อความรวดเร็ว
      const [profileRes, loansRes, finesRes] = await Promise.all([
        api.get(`/api/v1/members/${id}`),
        api.get(`/api/v1/members/${id}/loans`),
        api.get(`/api/v1/members/${id}/fines?status=UNPAID`)
      ]);

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
    } catch (error) {
      console.error("ดึงข้อมูลล้มเหลว:", error);
      setMessage({ type: 'error', text: `ไม่สามารถดึงข้อมูลโปรไฟล์ได้ (ID: ${id})` });
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });
    
    try {
      const response = await api.put(`/api/v1/members/${userId}`, formData);
      setProfile(response.data); 
      setIsEditing(false);
      setMessage({ type: 'success', text: 'บันทึกข้อมูลเรียบร้อยแล้ว!' });
    } catch (error) {
      console.error("อัปเดตข้อมูลล้มเหลว", error);
      setMessage({ type: 'error', text: 'กรุณาตรวจสอบข้อมูลอีกครั้ง' });
    }
  };

  if (isLoading) return <div className="p-10 font-bold text-center text-gray-600">กำลังโหลดข้อมูล...</div>;

  if (!profile) return (
    <div className="flex justify-center min-h-screen p-8 bg-gray-50">
      <div className="w-full max-w-3xl p-4 mt-10 text-red-800 bg-red-100 border border-red-200 rounded-md shadow-sm h-fit">
        {message.text}
      </div>
    </div>
  );

  // คำนวณยอดค่าปรับรวม
  const totalFines = fines.reduce((sum, fine) => sum + (fine.amount || 0), 0);

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-4xl mx-auto space-y-6">
        
        {/* กล่อง 1: ข้อมูลโปรไฟล์หลัก (ของเดิม) */}
        <div className="overflow-hidden bg-white border shadow-sm rounded-xl">
          <div className="p-6 text-white bg-blue-700">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold">{profile.username}</h1>
                <p className="text-blue-100">{profile.email}</p>
              </div>
              <div className="text-right">
                <span className="px-3 py-1 text-sm font-bold text-blue-700 bg-white rounded-full">
                  Tier: {profile.memberTier}
                </span>
                <p className="mt-2 text-sm text-blue-200">Role: {profile.role}</p>
              </div>
            </div>
          </div>

          <div className="p-8">
            {message.text && (
              <div className={`p-4 mb-6 text-sm rounded-md ${message.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                {message.text}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <div>
                  <label className="block mb-1 text-sm font-medium text-gray-700">ชื่อจริง</label>
                  <input 
                    type="text" 
                    disabled={!isEditing}
                    value={formData.firstName}
                    onChange={(e) => setFormData({...formData, firstName: e.target.value})}
                    className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                    required 
                  />
                </div>
                <div>
                  <label className="block mb-1 text-sm font-medium text-gray-700">นามสกุล</label>
                  <input 
                    type="text" 
                    disabled={!isEditing}
                    value={formData.lastName}
                    onChange={(e) => setFormData({...formData, lastName: e.target.value})}
                    className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                    required 
                  />
                </div>
              </div>

              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">เบอร์โทรศัพท์</label>
                <input 
                  type="text" 
                  disabled={!isEditing}
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})}
                  className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                />
              </div>

              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">ที่อยู่</label>
                <textarea 
                  disabled={!isEditing}
                  rows="3"
                  value={formData.address}
                  onChange={(e) => setFormData({...formData, address: e.target.value})}
                  className="w-full px-4 py-2 bg-gray-50 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:text-gray-500"
                ></textarea>
              </div>

              <div className="flex justify-end pt-4 space-x-4 border-t">
                {!isEditing ? (
                  <button 
                    type="button" 
                    onClick={() => setIsEditing(true)}
                    className="px-6 py-2 font-bold text-white transition bg-blue-600 rounded-md hover:bg-blue-700"
                  >
                    แก้ไขข้อมูล
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
                      ยกเลิก
                    </button>
                    <button 
                      type="submit" 
                      className="px-6 py-2 font-bold text-white transition bg-green-600 rounded-md hover:bg-green-700"
                    >
                      บันทึกข้อมูล
                    </button>
                  </>
                )}
              </div>
            </form>
          </div>
        </div>

        {/* กล่อง 2: แจ้งเตือนค่าปรับ (โผล่มาเฉพาะตอนมีค่าปรับค้างชำระ) */}
        {fines.length > 0 && (
          <div className="p-6 bg-white border border-red-200 shadow-sm rounded-xl">
            <h2 className="mb-4 text-xl font-bold text-red-700">ค่าปรับค้างชำระ (รวม: {totalFines} บาท)</h2>
            <ul className="space-y-2">
              {fines.map(fine => (
                <li key={fine.id} className="flex justify-between p-3 rounded-md bg-red-50">
                  <span className="text-red-800">{fine.reason || 'ค่าปรับส่งหนังสือล่าช้า'}</span>
                  <span className="font-bold text-red-700">{fine.amount} บาท</span>
                </li>
              ))}
            </ul>
            <p className="mt-4 text-sm text-gray-600">* กรุณาติดต่อบรรณารักษ์เพื่อชำระค่าปรับ การมียอดค้างชำระจะทำให้คุณไม่สามารถยืมหนังสือเพิ่มได้</p>
          </div>
        )}

        {/* กล่อง 3: ประวัติการยืม */}
        <div className="p-6 bg-white border shadow-sm rounded-xl">
          <h2 className="mb-4 text-xl font-bold text-gray-800">ประวัติการยืมหนังสือ</h2>
          {loans.length === 0 ? (
            <p className="py-4 text-center text-gray-500">ยังไม่มีประวัติการยืมหนังสือ</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-gray-100 border-b">
                    <th className="p-3 text-sm font-semibold text-gray-700">รหัสใบยืม</th>
                    <th className="p-3 text-sm font-semibold text-gray-700">หนังสือ</th>
                    <th className="p-3 text-sm font-semibold text-gray-700">วันที่ยืม</th>
                    <th className="p-3 text-sm font-semibold text-gray-700">สถานะ</th>
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
                        {loan.loanDate ? new Date(loan.loanDate).toLocaleDateString('th-TH') : '-'}
                      </td>
                      <td className="p-3 text-sm">
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full 
                          ${loan.status === 'ACTIVE' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'}`}>
                          {loan.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}