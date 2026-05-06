import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate} from 'react-router-dom';
import { movieService } from './api';
import './App.css';

import Home from './Home/Home.jsx';
import MovieDetail from './MovieDetail/MovieDetail.jsx';
import Profile from './Profile/Profile.jsx';
import Header from './Header/Header.jsx';
import AdminPage from "./AdminPage/AdminPage.jsx";
import Sidebar from "./Sidebar/Sidebar.jsx";
import AuthPage from "./AuthPage/AuthPage.jsx";
import Genres from "./Genres/Genres.jsx";

export default function App() {
    const [currentUser, setCurrentUser] = useState(() => {
        const saved = localStorage.getItem("userData");
        try {
            return saved ? JSON.parse(saved) : null;
        } catch (e) {
            return null;
        }
    });

    const [isAuthChecking, setIsAuthChecking] = useState(true);
    const navigate = useNavigate();
    const isAdmin = currentUser?.role === 'ROLE_ADMIN';

    useEffect(() => {
        const initAuth = async () => {
            try {
                const res = await movieService.getCurrentUser();
                if (res && res.data) {
                    setCurrentUser(res.data);
                }
            } catch (err) {
                if (movieService.logout) movieService.logout();
            } finally {
                setIsAuthChecking(false);
            }
        };
        initAuth();
    }, []);

    const handleLogout = () => {
        movieService.logout();
        setCurrentUser(null);
        navigate('/');
    };

    if (isAuthChecking) return (
        <div className="loading-screen">
            <h1>Загрузка сервиса...</h1>
        </div>
    );

    return (
        <>
            {!currentUser ? (
                <Routes>
                    <Route path="*" element={<AuthPage onLogin={setCurrentUser} />} />
                </Routes>
            ) : (
                <div className="app-layout">
                    <Sidebar isAdmin={isAdmin} />
                    <div className="main-content">
                        <Header currentUser={currentUser} handleLogout={handleLogout} />
                        <Routes>
                            <Route path="/" element={<Home isAdmin={isAdmin} title="Главная" />} />
                            {isAdmin && <Route path="/admin" element={<AdminPage />} />}
                            <Route path="/new" element={<Home isAdmin={isAdmin} title="Новинки" />} />
                            <Route path="/popular" element={<Home isAdmin={isAdmin} title="Популярное" />} />
                            <Route path="/genres" element={<Genres isAdmin={isAdmin} />} />
                            <Route path="/profile" element={<Profile currentUser={currentUser} handleLogout={handleLogout} />} />
                            <Route path="/profile/:targetUserId" element={<Profile currentUser={currentUser} />} />
                            <Route path="/movie/:id" element={<MovieDetail isAdmin={isAdmin} currentUser={currentUser} />} />
                        </Routes>
                    </div>
                </div>
            )}
        </>
    );
}