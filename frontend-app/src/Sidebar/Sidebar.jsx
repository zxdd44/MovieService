import {Link, useLocation} from "react-router-dom";
import React from "react";
import '../App.css';

function Sidebar ({ isAdmin }) {
    const location = useLocation();

    const navItems = [
        { path: '/profile', label: 'Профиль' },
        { path: '/', label: 'Главная' },
        { path: '/genres', label: 'Жанры' },
        { path: '/new', label: 'Новинки' },
        { path: '/popular', label: 'Популярное' },
    ];

    return (
        <div className="sidebar">
            <div className="sidebar-nav">
                {navItems.map(item => (
                    <Link
                        key={item.path}
                        to={item.path}
                        className={`nav-button nav-link ${location.pathname === item.path ? 'active' : ''}`}
                    >
                        {item.label}
                    </Link>
                ))}

                {isAdmin && (
                    <Link
                        to="/admin"
                        className={`nav-button nav-link admin-link ${location.pathname === '/admin' ? 'active' : ''}`}
                    >
                        Для админа
                    </Link>
                )}
            </div>
        </div>
    );
}

export default Sidebar;