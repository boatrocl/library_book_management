import { useState, useEffect } from 'react';
import api from '../api';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    address: ''
  });

  //เนื่องจากระบบ Auth ยังไม่เสร็จ เลยใช้ const userId = 1; ไปก่อนนะจ๊ะ
  //เมื่อเสร็จเราค่อยดึง ID จาก Token (jwt-decode) มาแทนที่ตรงนี้
  const userId = 1; 

  const fetchProfile = async () => {
    try {
      const response = await api.get(`/api/v1/members/${userId}`);
      setProfile(response.data);
      setFormData({
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        phoneNumber: response.data.phoneNumber || '',
        address: response.data.address || ''
      });
    } catch (error) {
      console.error("ดึงข้อมูลโปรไฟล์ล้มเหลว", error);
      setMessage({ type: 'error', text: 'ไม่สามารถดึงข้อมูลได้' });
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: '', text: '' });
    
    try {
      const response = await api.put(`/api/v1/members/${userId}`, formData);
      setProfile(response.data); //อัปเดตข้อมูลบนหน้าจอด้วยข้อมูลที่ส่งกลับมาจาก Backend
      setIsEditing(false);
      setMessage({ type: 'success', text: 'บันทึกข้อมูลเรียบร้อยแล้ว!' });
    } catch (error) {
      console.error("อัปเดตข้อมูลล้มเหลว", error);
      setMessage({ type: 'error', text: 'กรุณาตรวจสอบข้อมูลอีกครั้ง' });
    }
  };

  if (!profile) return <div className="p-10 text-center">กำลังโหลดข้อมูล...</div>;

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-3xl mx-auto bg-white border rounded-xl shadow-sm overflow-hidden">
        
        {/*ส่วนหัวโปรไฟล์*/}
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

        {/*ส่วนฟอร์มข้อมูล*/}
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
                  className="px-6 py-2 font-bold text-white bg-blue-600 rounded-md hover:bg-blue-700 transition"
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
                      }); //รีเซ็ตค่ากลับไปเป็นของเดิม
                    }}
                    className="px-6 py-2 font-bold text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 transition"
                  >
                    ยกเลิก
                  </button>
                  <button 
                    type="submit" 
                    className="px-6 py-2 font-bold text-white bg-green-600 rounded-md hover:bg-green-700 transition"
                  >
                    บันทึกข้อมูล
                  </button>
                </>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}