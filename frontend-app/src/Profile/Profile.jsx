import {useNavigate, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import '../App.css';
import {movieService} from "../api.jsx";

function Profile ({ currentUser, handleLogout }) {
    const { targetUserId } = useParams();
    const [profileInfo, setProfileInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const isNotMe = Boolean(targetUserId && targetUserId !== String(currentUser?.id));
    const userId = isNotMe ? targetUserId : currentUser?.id;
    const isAdmin = currentUser?.role === 'ROLE_ADMIN';

    const [editForm, setEditForm] = useState({
        username: '',
        status: '',
        avatarUrl: ''
    });

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

    useEffect(() => {
        if (!userId) return;
        setLoading(true);
        movieService.getProfile(userId)
            .then((userRes) => {
                setProfileInfo(userRes.data);
                if (!isNotMe) {
                    setEditForm({
                        username: userRes.data.username || '',
                        status: userRes.data.status || '',
                        avatarUrl: userRes.data.avatarUrl || ''
                    });
                }
            })
            .finally(() => setLoading(false));
    }, [userId, isNotMe]);

    const handleBlockUser = () => {
        if (window.confirm(`Вы уверены, что хотите заблокировать ${profileInfo.username}?`)) {
            movieService.deleteUser(profileInfo.id)
                .then(() => {
                    navigate('/');
                })
        }
    };

    const handleDeleteMyProfile = async () => {
        if (window.confirm("Вы уверены? Это действие удалит ваш аккаунт навсегда.")) {
            try {
                await movieService.deleteUser(currentUser.id);
                handleLogout();
                navigate('/');
            } catch (err) {
                alert("Ошибка при удалении профиля");
            }
        }
    };

    const handleSaveProfile = async () => {
        try {
            const response = await movieService.updateUser(editForm);
            setProfileInfo(response.data);
            const updatedUser = {
                ...currentUser,
                username: response.data.username,
                avatarUrl: response.data.avatarUrl,
                status: response.data.status
            };
            localStorage.setItem("userData", JSON.stringify(updatedUser));
            if (typeof setCurrentUser === 'function') {
                setCurrentUser(updatedUser);
            }
            setIsEditModalOpen(false);
        } catch (err) {
            alert("Ошибка при обновлении профиля");
        }
    };

    if (loading) return <div className="profile-main">Загрузка...</div>;
    if (!profileInfo) return <div className="profile-main">Профиль не найден.</div>;

    const chart = profileInfo.statusChart || {};
    const watchedCount = chart.WATCHED?.count || 0;
    const watchedPercent = chart.WATCHED?.percentage || 0;
    const deferredCount = chart.DEFERRED?.count || 0;
    const deferredPercent = chart.DEFERRED?.percentage || 0;
    const abandonedCount = chart.ABANDONED?.count || 0;
    const abandonedPercent = chart.ABANDONED?.percentage || 0;
    const total = watchedCount + deferredCount + abandonedCount;

    return (
        <div className="page-container">
        <div className="profile-main">
            <div className="profile-header-flex">
                <div className="avatar-wrapper">
                    <div className="avatar-circle">
                        <img className="avatar-img" src={profileInfo.avatarUrl || "https://via.placeholder.com/100"} alt="avatar" />
                    </div>
                    {!isNotMe ? (
                        <button className="nav-button small" onClick={() => setIsEditModalOpen(true)}>⚙️ Настройки</button>
                    ) : isAdmin && (
                        <button className="nav-button delete" onClick={handleBlockUser}>🚫 Блок</button>
                    )}
                </div>
                <div className="profile-name-section">
                    <h2>{profileInfo.username}</h2>
                    <p className="profile-status-text">{profileInfo.status || "Нет статуса"}</p>
                </div>
            </div>

            {isEditModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3>Настройки профиля</h3>
                        <label className="form-label">Имя</label>
                        <input className="search-bar" value={editForm.username} onChange={e => setEditForm({...editForm, username: e.target.value})} />
                        <label className="form-label">Статус</label>
                        <input className="search-bar" value={editForm.status} onChange={e => setEditForm({...editForm, status: e.target.value})} />
                        <label className="form-label">Фото профиля</label>
                        <input className="search-bar" value={editForm.avatarUrl} onChange={e => setEditForm({...editForm, avatarUrl: e.target.value})} />
                        <div style={{display: 'flex', gap: '10px', marginTop: '10px'}}>
                            <button className="nav-button active"
                                    style={{ padding: '14px 20px', fontSize: '16px', flex: 1 }}
                                    onClick={handleSaveProfile}>💾 Сохранить</button>
                            <button className="nav-button"
                                    style={{ padding: '14px 20px', fontSize: '16px', flex: 1 }}
                                    onClick={() => setIsEditModalOpen(false)}>Отмена</button>
                        </div>
                    </div>
                </div>
            )}

            <div className="stats-container">
                <h3 style={{textAlign: 'center'}}>Статистика</h3>

                <div className="pie-chart" style={{
                    background: total > 0
                        ? `conic-gradient(#4caf50 0% ${watchedPercent}%, #ff9800 ${watchedPercent}% ${watchedPercent + deferredPercent}%, #f44336 ${watchedPercent + deferredPercent}% 100%)`
                        : '#e0e0e0',
                }}></div>

                <div className="stats-legend">
                    <p className="legend-item">🟢 Просмотрено: {watchedCount} ({watchedPercent.toFixed(1)}%)</p>
                    <p className="legend-item">🟠 Отложено: {deferredCount} ({deferredPercent.toFixed(1)}%)</p>
                    <p className="legend-item">🔴 Брошено: {abandonedCount} ({abandonedPercent.toFixed(1)}%)</p>
                </div>
            </div>

            <h3 style={{marginTop: '40px'}}>Недавние просмотренные</h3>
            <div className="recent-movies-scroll">
                {profileInfo.watchedMovies?.length > 0 ? profileInfo.watchedMovies.map(movie => (
                    <div key={movie.id} className="recent-card" onClick={() => navigate(`/movie/${movie.id}`)}>
                        <div className="recent-poster-thumb">
                            <img
                                src={movie.imageUrl || "https://ir.ozone.ru/s3/multimedia-1-e/7579533542.jpg"}
                                className="avatar-img"
                                alt="постер"
                            />
                        </div>
                        <p className="recent-title">{movie.title}</p>
                        <p style={{fontSize: '11px', color: '#888'}}>{movie.year}</p>
                    </div>
                )) : <p className="profile-status-text">Список пуст</p>}
            </div>

            {!isNotMe && !isAdmin && (
                <div className="danger-zone">
                    <button className="delete-account-btn" onClick={handleDeleteMyProfile}>Удалить аккаунт</button>
                </div>
            )}
        </div>
        </div>
    );
}

export default Profile;