import axios from 'axios';

const API_BASE_URL =
    import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

// Interceptor สำหรับ Request
// ก่อนที่ข้อมูลจะวิ่งออกจาก Frontend ให้ค้นหา Token ในกระเป๋า localStorage
// ถ้าเจอให้แปะติดไปด้วย
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

// Interceptor สำหรับ Response
// ตรวจสอบข้อมูลที่ Backend ตอบกลับมา
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const { response, config } = error;

        if (response) {
            // A 403 means the user is authenticated but lacks permission; keep the session intact.
            if (response.status === 401 && config?.url !== '/api/v1/auth/login') {
                console.warn('เซสชันหมดอายุหรือไม่ถูกต้อง');

                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        }

        return Promise.reject(error);
    }
);

export default api;
