import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import '../App.css';

function Header ({ currentUser, handleLogout }) {
    const [searchTerm, setSearchTerm] = useState("");
    const navigate = useNavigate();

    const handleSearch = (e) => {
        if (e.key === 'Enter') {
            const query = searchTerm.trim();
            navigate(query ? `/?search=${encodeURIComponent(query)}` : `/`);
        }
    };

    const onLogoutClick = () => {
        if (handleLogout) {
            handleLogout();
            navigate('/');
        }
    };

    return (
        <div className="top-bar">
            <div className="spacer"></div>

            <input
                type="text"
                className="search-bar"
                placeholder="Поиск по названию или режиссеру..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={handleSearch}/>
            <div
                className={`logout-btn ${currentUser ? 'authenticated' : 'guest'}`}
                onClick={onLogoutClick}>
                Выйти
            </div>
        </div>
    );
}

export default Header;