import axios from 'axios';

const API = axios.create({
    baseURL: 'https://movieservice-production-dd97.up.railway.app/api',
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true
});

API.interceptors.request.use(config => {
    const authHeader = localStorage.getItem('userAuth');
    if (authHeader) {
        config.headers.Authorization = authHeader;
    }
    return config;
});

API.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            console.warn("Сессия недействительна. Очистка кэша...");
            localStorage.removeItem('userAuth');
            localStorage.removeItem('userData');
            if (window.location.pathname !== '/') {
                window.location.href = '/';
            }
        }
        return Promise.reject(error);
    }
);

export const movieService = {
    login: async (credentials) => {
        const authHeader = 'Basic ' + btoa(`${credentials.username}:${credentials.password}`);
        const res = await API.post('/auth/login', {}, {
            headers: { 'Authorization': authHeader }
        });
        if (res.data) {
            localStorage.setItem('userAuth', authHeader);
            localStorage.setItem('userData', JSON.stringify(res.data));
        }
        return res;
    },

    getCurrentUser: async () => {
        const userAuth = localStorage.getItem('userAuth');
        if (!userAuth) return null;
        return API.get('/users/me');
    },

    logout: () => {
        localStorage.removeItem('userAuth');
        localStorage.removeItem('userData');
        localStorage.removeItem('token');
    },

    register: (userData) => API.post('/auth/register', userData),
    searchMovies: (params) => API.get('/movies/search', { params }),
    getById: (id) => API.get(`/movies/${id}`),
    create: (data) => API.post('/movies', data),
    update: (id, data) => API.put(`/movies/${id}`, data),
    updateProgress: (movieId, statusIndex) =>
        API.put(`/movies/${movieId}/progress?status=${statusIndex}`),
    delete: (id) => API.delete(`/movies/${id}`),
    getProfile: (userId) => API.get(`/movies/users/${userId}/public-profile`),
    updateUser: (userData) => {
        const userDataStr = localStorage.getItem('userData');
        if (!userDataStr) throw new Error("Пользователь не найден в локальном хранилище");
        const user = JSON.parse(localStorage.getItem('userData'));
        return API.put(`/users/${user.id}`, userData);
    },
    deleteUser: (userId) => API.delete(`/movies/users/${userId}`),
    addReview: (movieId, data) => API.post(`/movies/${movieId}/reviews`, data),
    deleteReview: (reviewId) => API.delete(`/movies/reviews/${reviewId}`),
};