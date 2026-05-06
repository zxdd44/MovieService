import React, {useState} from "react";
import {movieService} from "../api.jsx";
import '../App.css';

function AuthPage ({ onLogin }) {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({ username: '', password: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (isLogin) {
                const res = await movieService.login(formData);
                if (res.data?.id) onLogin(res.data);
            } else {
                await movieService.register(formData);
                alert("Регистрация успешна!");
                setIsLogin(true);
            }
        } catch (err) { alert("Ошибка авторизации"); }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 style={{ textAlign: 'center', marginBottom: '30px', fontWeight: 'bold' }}>
                    {isLogin ? 'Вход' : 'Регистрация'}
                </h2>
                <form onSubmit={handleSubmit} className="auth-form">
                    <input className="search-bar" placeholder="Логин" value={formData.username}
                           onChange={e => setFormData({...formData, username: e.target.value})} required />
                    <input className="search-bar" type="password" placeholder="Пароль" value={formData.password}
                           onChange={e => setFormData({...formData, password: e.target.value})} required />
                    <button type="submit" className="nav-button active">
                        {isLogin ? 'Войти' : 'Создать аккаунт'}
                    </button>
                </form>
                <p className="auth-switch-text" style={{ textAlign: 'center', marginTop: '15px' }}>
                    {isLogin ? 'Нет аккаунта? ' : 'Уже есть аккаунт? '}
                    <span className="auth-link-blue" onClick={() => setIsLogin(!isLogin)}>
            {isLogin ? 'Зарегистрироваться' : 'Войти'}
        </span>
                </p>
            </div>
        </div>
    );
}

export default AuthPage;