import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login';

function App() {
  return (
    <Routes>
      {/* ถ้าผู้ใช้พิมพ์ /login ให้แสดงหน้า Login */}
      <Route path="/login" element={<Login />} />
      
      {/* ถ้าผู้ใช้เข้ามาหน้าแรก (/) ให้แสดงข้อความต้อนรับชั่วคราวก่อน */}
      <Route path="/" element={
        <div className="p-10 text-center">
          <h1 className="text-3xl font-bold">ยินดีต้อนรับสู่หน้าหลัก!</h1>
          <p className="mt-4 text-gray-600">คุณล็อกอินสำเร็จแล้ว (เดี๋ยวเราจะมาทำหน้า Catalog ตรงนี้)</p>
        </div>
      } />
    </Routes>
  );
}

export default App;