import { Routes, Route, useLocation } from 'react-router-dom';
import Login from './pages/Login';
import Catalog from './pages/Catalog';
import Navbar from './components/Navbar';

function App() {
  const location = useLocation();
  
  //เช็กว่าปัจจุบันเป็น/login
  const isLoginPage = location.pathname === '/login';

  return (
    <div className="min-h-screen bg-gray-50">
      {/*ถ้าไม่ใช่ Login ให้แสดง Navbar*/}
      {!isLoginPage && <Navbar />}
      
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Catalog />} />
      </Routes>
    </div>
  );
}

export default App;