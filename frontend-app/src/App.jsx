import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate, useLocation, useParams } from 'react-router-dom';
import { movieService } from './api';
import './App.css';

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

    const isAdmin = currentUser?.role === 'ROLE_ADMIN';

    useEffect(() => {
        const initAuth = async () => {
            try {
                const res = await movieService.getCurrentUser();
                if (res && res.data) {
                    setCurrentUser(res.data);
                }
            } catch (err) {
                console.error("Сессия истекла");
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
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <h1 style={{ color: '#333' }}>Загрузка сервиса...</h1>
        </div>
    );

    return (
        <>
            {!currentUser ? (
                <Routes>
                    <Route path="*" element={<AuthPage onLogin={setCurrentUser} />} />
                </Routes>
            ) : (
                <div className="app-layout" style={{ display: 'flex', alignItems: 'flex-start' }}>
                    <Sidebar isAdmin={isAdmin} onLogout={handleLogout} />

                    <div className="main-content" style={{ flex: 1, width: '100%' }}>
                        <Header currerntUser={currentUser} handleLogout={handleLogout} />
                        <Routes>
                            <Route path="/" element={<Home isAdmin={isAdmin} title="Главная" />} />
                            <Route path="/new" element={<Home isAdmin={isAdmin} title="Новинки" />} />
                            <Route path="/popular" element={<Home isAdmin={isAdmin} title="Популярное" />} />
                            <Route path="/genres" element={<Genres isAdmin={isAdmin} />} />
                            <Route path="/profile" element={<Profile currentUser={currentUser} />} />
                            <Route path="/profile/:targetUserId" element={<Profile currentUser={currentUser} />} />
                            <Route path="/movie/:id" element={<MovieDetail isAdmin={isAdmin} currentUser={currentUser} />} />
                        </Routes>
                    </div>
                </div>
            )}
        </>
    );
}

function AuthPage({ onLogin }) {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({ username: '', password: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (isLogin) {
                const res = await movieService.login(formData);
                const userData = res.data;
                if (userData && userData.id) {
                    onLogin(userData);
                } else {
                    throw new Error("Данные пользователя не получены");
                }
            } else {
                await movieService.register(formData);
                alert("Регистрация успешна! Теперь войдите.");
                setIsLogin(true);
            }
        } catch (err) {
            console.error("Детальная ошибка:", err);
            alert("Ошибка: " + (err.response?.status === 404 ? "Путь /api/auth/login не найден" : err.message));
        }
    };

    return (
        <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f0f2f5' }}>
            <div style={{ background: 'white', padding: '40px', borderRadius: '20px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)', width: '350px' }}>
                <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>{isLogin ? 'Вход в сервис' : 'Регистрация'}</h2>

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <input
                        className="search-bar"
                        placeholder="Логин"
                        value={formData.username}
                        onChange={e => setFormData({...formData, username: e.target.value})}
                        required
                    />
                    <input
                        className="search-bar"
                        type="password"
                        placeholder="Пароль"
                        value={formData.password}
                        onChange={e => setFormData({...formData, password: e.target.value})}
                        required
                    />
                    <button className="nav-button" style={{ background: '#333', color: 'white', padding: '12px' }}>
                        {isLogin ? 'Войти' : 'Создать аккаунт'}
                    </button>
                </form>

                <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '14px' }}>
                    {isLogin ? 'Нет аккаунта?' : 'Уже есть аккаунт?'}
                    <span
                        style={{ color: '#3498db', cursor: 'pointer', marginLeft: '5px' }}
                        onClick={() => setIsLogin(!isLogin)}
                    >
                        {isLogin ? 'Зарегистрироваться' : 'Войти'}
                    </span>
                </p>
            </div>
        </div>
    );
}

function Sidebar({ isAdmin, setIsAdmin }) {
    const location = useLocation();
    const navItems = [
        { path: '/profile', label: 'Профиль' },
        { path: '/', label: 'Главная' },
        { path: '/genres', label: 'Жанры' },
        { path: '/new', label: 'Новинки' },
        { path: '/popular', label: 'Популярное' },
    ];

    return (
        <div className="sidebar" style={{
            position: 'sticky',
            top: 0,
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            padding: '20px',
            boxSizing: 'border-box',
            borderRight: '1px solid #ddd',
            minWidth: '200px'
        }}>
            <div>
                {navItems.map(item => (
                    <Link
                        key={item.path}
                        to={item.path}
                        className={`nav-button ${location.pathname === item.path ? 'active' : ''}`}
                        style={{ display: 'block', marginBottom: '10px' }}
                    >
                        {item.label}
                    </Link>
                ))}
            </div>

            <div style={{ marginTop: 'auto' }}>
                <button
                    onClick={() => setIsAdmin(!isAdmin)}
                    className="nav-button"
                    style={{ width: '100%', background: isAdmin ? '#f39c12' : '#333', color: 'white' }}
                >
                    Режим: {isAdmin ? 'Админ' : 'Пользователь'}
                </button>
            </div>
        </div>
    );
}

function Header({ currentUser, handleLogout }) {
    const [searchTerm, setSearchTerm] = useState("");
    const navigate = useNavigate();

    const handleSearch = (e) => {
        if (e.key === 'Enter') {
            const query = searchTerm.trim();
            if (query) {
                navigate(`/?search=${encodeURIComponent(query)}`);
            } else {
                navigate(`/`);
            }
        }
    };

    return (
        <div className="top-bar">
            <div></div>
            <input
                type="text"
                className="search-bar"
                placeholder="Поиск по названию или режиссеру..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={handleSearch}
            />

            <div className="auth-link"
                 onClick={() => {
                     if (handleLogout) {
                         handleLogout();
                         navigate('/');
                     }
                 }}
                 style={{cursor: 'pointer', color: currentUser ? '#e74c3c' : '#333', fontWeight: 'bold'}}
            >
                Выйти
            </div>
        </div>
    );
}

function Home({ isAdmin, title }) {
    const [movies, setMovies] = useState([]);
    const [pageSize, setPageSize] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingMovie, setEditingMovie] = useState(null);
    const [formData, setFormData] = useState({ title: '', year: 2024, director: '', genres: '', imageUrl: '' });
    const navigate = useNavigate();
    const location = useLocation();

    const fetchMovies = async () => {
        const params = new URLSearchParams(location.search);
        const searchQuery = params.get('search');

        try {
            if (searchQuery) {
                const [resT, resD] = await Promise.all([
                    movieService.searchMovies({ title: searchQuery, size: 50 }),
                    movieService.searchMovies({ director: searchQuery, size: 50 })
                ]);

                const getMoviesFromResponse = (res) => {
                    if (res.data && Array.isArray(res.data.content)) return res.data.content;
                    if (Array.isArray(res.data)) return res.data;
                    return [];
                };

                const foundT = getMoviesFromResponse(resT);
                const foundD = getMoviesFromResponse(resD);
                const found = [...foundT, ...foundD];
                let results = Array.from(new Map(found.map(m => [m.id, m])).values());
                if (title === "Новинки") {
                    results.sort((a, b) => b.year - a.year);
                } else if (title === "Популярное") {
                    results.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
                }
                setMovies(results);
                setTotalElements(results.length);
            } else {
                let mainRes = await movieService.searchMovies({ size: pageSize });
                let baseList = (mainRes.data && mainRes.data.content)
                    ? mainRes.data.content
                    : (Array.isArray(mainRes.data) ? mainRes.data : []);
                setTotalElements(mainRes.data.totalElements || baseList.length);
                if (title === "Новинки") {
                    baseList.sort((a, b) => b.year - a.year);
                } else if (title === "Популярное") {
                    baseList.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
                }
                setMovies(baseList);

            }
        } catch (err) {
            console.error("Ошибка загрузки:", err);
        }
    };

    useEffect(() => {
        fetchMovies();
    }, [title, pageSize, location.search]);

    const handleDelete = async (e, movieId) => {
        e.stopPropagation();
        if (window.confirm("Вы уверены, что хотите удалить этот фильм?")) {
            try {
                await movieService.delete(movieId);
                fetchMovies();
            } catch (err) {
                alert("Ошибка при удалении фильма.");
            }
        }
    };

    const handleLoadMore = () => {
        setPageSize(prev => prev + 10);
    };

    const handleEditSubmit = (e) => {
        e.preventDefault();
        const updatedMovie = {
            ...editingMovie,
            genres: typeof editingMovie.genres === 'string'
                ? editingMovie.genres.split(',').map(g => g.trim()).filter(Boolean)
                : editingMovie.genres
        };

        movieService.update(editingMovie.id, updatedMovie)
            .then(() => {
                setEditingMovie(null);
                fetchMovies();
            })
            .catch(err => alert("Ошибка обновления. Проверьте консоль."));
    };

    const handleCreate = (e) => {
        e.preventDefault();
        if (!formData.title || !formData.director) return alert("Заполните основные поля!");

        const newMovie = {
            ...formData,
            status: 0,
            genres: formData.genres.split(',').map(g => g.trim()).filter(Boolean)
        };

        movieService.create(newMovie)
            .then(() => {
                setIsModalOpen(false);
                setFormData({ title: '', year: 2024, director: '', genres: '', imageUrl: '' });
                fetchMovies();
            })
            .catch(err => alert("Ошибка: " + (err.response?.data?.message || "не удалось создать")));
    };

    return (
        <div>
            <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '20px'}}>
                <h2>{title}
                    {location.search && <span style={{fontSize: '16px', color: '#27ae60', marginLeft: '10px'}}>— Результаты поиска</span>}
                </h2>
                {isAdmin && <button className="nav-button" onClick={() => setIsModalOpen(true)}>➕ Создать фильм</button>}
            </div>

            {isModalOpen && (
                <div style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000}}>
                    <form className="profile-container" style={{width: '400px', display: 'flex', flexDirection: 'column', gap: '10px'}} onSubmit={handleCreate}>
                        <h3>Новый фильм</h3>
                        <input type="text" placeholder="Название" className="search-bar" style={{margin: 0}} value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} />
                        <input type="number" placeholder="Год" className="search-bar" style={{margin: 0}} value={formData.year} onChange={e => setFormData({...formData, year: e.target.value})} />
                        <input type="text" placeholder="Режиссер" className="search-bar" style={{margin: 0}} value={formData.director} onChange={e => setFormData({...formData, director: e.target.value})} />
                        <input type="text" placeholder="Жанры (через запятую)" className="search-bar" style={{margin: 0}} value={formData.genres} onChange={e => setFormData({...formData, genres: e.target.value})} />
                        <input type="text" placeholder="Ссылка на постер (URL)" className="search-bar" style={{margin: 0}} value={formData.imageUrl} onChange={e => setFormData({...formData, imageUrl: e.target.value})} />
                        <div style={{display: 'flex', gap: '10px', marginTop: '10px'}}>
                            <button type="submit" className="nav-button" style={{flex: 1, background: '#333', color: 'white'}}>Создать</button>
                            <button type="button" className="nav-button" style={{flex: 1}} onClick={() => setIsModalOpen(false)}>Отмена</button>
                        </div>
                    </form>
                </div>
            )}

            {editingMovie && (
                <div style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1100}}>
                    <form className="profile-container" style={{width: '420px', display: 'flex', flexDirection: 'column', gap: '15px'}} onSubmit={handleEditSubmit}>
                        <h3 style={{marginTop: 0}}>Изменить информацию</h3>

                        <label style={{display: 'flex', flexDirection: 'column', gap: '5px'}}>
                            <span style={{fontSize: '14px', fontWeight: 'bold', color: '#555'}}>Название:</span>
                            <input type="text" className="search-bar" style={{margin: 0}} value={editingMovie.title || ''} onChange={e => setEditingMovie({...editingMovie, title: e.target.value})} />
                        </label>

                        <label style={{display: 'flex', flexDirection: 'column', gap: '5px'}}>
                            <span style={{fontSize: '14px', fontWeight: 'bold', color: '#555'}}>Год выхода:</span>
                            <input type="number" className="search-bar" style={{margin: 0}} value={editingMovie.year || ''} onChange={e => setEditingMovie({...editingMovie, year: e.target.value})} />
                        </label>

                        <label style={{display: 'flex', flexDirection: 'column', gap: '5px'}}>
                            <span style={{fontSize: '14px', fontWeight: 'bold', color: '#555'}}>Режиссёр:</span>
                            <input type="text" className="search-bar" style={{margin: 0}} value={editingMovie.director || ''} onChange={e => setEditingMovie({...editingMovie, director: e.target.value})} />
                        </label>

                        <label style={{display: 'flex', flexDirection: 'column', gap: '5px'}}>
                            <span style={{fontSize: '14px', fontWeight: 'bold', color: '#555'}}>Жанры (через запятую):</span>
                            <input type="text" className="search-bar" style={{margin: 0}} value={editingMovie.genres || ''} onChange={e => setEditingMovie({...editingMovie, genres: e.target.value})} />
                        </label>

                        <label style={{display: 'flex', flexDirection: 'column', gap: '5px'}}>
                            <span style={{fontSize: '14px', fontWeight: 'bold', color: '#555'}}>Ссылка на постер (URL):</span>
                            <input type="text" className="search-bar" style={{margin: 0}} value={editingMovie.imageUrl || ''} onChange={e => setEditingMovie({...editingMovie, imageUrl: e.target.value})} />
                        </label>

                        <div style={{display: 'flex', gap: '10px', marginTop: '10px'}}>
                            <button type="submit" className="nav-button" style={{flex: 1, background: '#333', color: 'white'}}>💾 Сохранить</button>
                            <button type="button" className="nav-button" style={{flex: 1}} onClick={() => setEditingMovie(null)}>Отмена</button>
                        </div>
                    </form>
                </div>
            )}

            <div className="movie-grid" style={{display: 'flex', flexWrap: 'wrap', gap: '20px'}}>
                {movies.length > 0 ? movies.map(movie => (
                    <div key={movie.id}
                         className="movie-card"
                         style={{position: 'relative', width: '420px', display: 'flex', gap: '15px', background: 'white', border: '1px solid #ddd', borderRadius: '12px', padding: '15px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', cursor: 'pointer'}}
                         onClick={() => navigate(`/movie/${movie.id}`)}>

                        {isAdmin && (
                            <div style={{position: 'absolute', top: '10px', right: '10px', zIndex: 10, display: 'flex', gap: '5px'}}>
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setEditingMovie({...movie, genres: movie.genres?.join(', ')});
                                    }}
                                    style={{background: 'white', border: '1px solid #ccc', borderRadius: '50%', width: '30px', height: '30px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 0}}
                                >✏️</button>
                                <button
                                    onClick={(e) => handleDelete(e, movie.id)}
                                    style={{background: 'white', border: '1px solid #ff4d4f', borderRadius: '50%', width: '30px', height: '30px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 0}}
                                >🗑️</button>
                            </div>
                        )}

                        <div style={{width: '140px', height: '210px', background: '#eee', borderRadius: '8px', overflow: 'hidden', flexShrink: 0, border: '1px solid #ddd'}}>
                            {movie.imageUrl ? <img src={movie.imageUrl} style={{width:'100%', height:'100%', objectFit:'cover'}} alt="постер" /> : <div style={{display:'flex', height:'100%', alignItems:'center', justifyContent:'center', color: '#999'}}>Постер</div>}
                        </div>

                        <div style={{flex: 1, display: 'flex', flexDirection: 'column'}}>
                            <h3 style={{margin: '0 0 10px 0', paddingRight: '25px', fontSize: '18px', lineHeight: '1.2'}}>{movie.title}</h3>
                            <p style={{margin: '0 0 5px 0', fontSize: '14px'}}><strong>Режиссёр:</strong> {movie.director}</p>
                            <p style={{margin: '0 0 5px 0', fontSize: '14px'}}><strong>Год:</strong> {movie.year}</p>
                            <p style={{margin: '0 0 15px 0', fontSize: '14px'}}><strong>Жанры:</strong> {movie.genres?.join(', ')}</p>
                            <div style={{marginTop: 'auto', display: 'flex', alignItems: 'center', gap: '5px'}}>
                                <span style={{color: '#f39c12', fontSize: '18px'}}>⭐</span>
                                <span style={{fontWeight: 'bold', fontSize: '16px'}}>
                                    {movie.averageRating ? movie.averageRating.toFixed(1) : "0.0"}</span>
                                <span style={{color: '#888', fontSize: '14px'}}>/ 5</span>
                            </div>
                        </div>
                    </div>
                )) : (
                    <div style={{textAlign: 'center', width: '100%', marginTop: '50px'}}>
                        <p style={{ color: '#999', fontSize: '18px' }}>Ничего не нашли по вашему запросу 🔍</p>
                    </div>
                )}
            </div>

            {movies.length < totalElements && !location.search && (
                <div style={{ textAlign: 'center', marginTop: '30px' }}>
                    <button className="nav-button" onClick={handleLoadMore} style={{ padding: '10px 40px', background: '#333', color: 'white' }}>
                        Показать еще
                    </button>
                </div>
            )}
        </div>
    );
}

function Profile({ currentUser, handleLogout }) {
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
            .catch((err) => {
                console.error("Ошибка профиля:", err);
            })
            .finally(() => setLoading(false));
    }, [userId, isNotMe]);

    const handleBlockUser = () => {
        if (window.confirm(`Вы уверены, что хотите заблокировать ${profileInfo.username}?`)) {
            movieService.deleteUser(profileInfo.id)
                .then(() => {
                    navigate('/');
                })
                .catch(() => alert("Ошибка удаления"));
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
            alert("Данные сохранены в базе!");
        } catch (err) {
            alert("Ошибка при обновлении профиля");
        }
    };

    if (loading) return <div style={{padding: '20px'}}>Загрузка профиля...</div>;
    if (!profileInfo) return <div style={{padding: '20px'}}>Профиль не найден.</div>;

    const chart = profileInfo.statusChart || {};
    const watchedCount = chart.WATCHED?.count || 0;
    const watchedPercent = chart.WATCHED?.percentage || 0;
    const deferredCount = chart.DEFERRED?.count || 0;
    const deferredPercent = chart.DEFERRED?.percentage || 0;
    const abandonedCount = chart.ABANDONED?.count || 0;
    const abandonedPercent = chart.ABANDONED?.percentage || 0;
    const total = watchedCount + deferredCount + abandonedCount;
    const watchedMovies = profileInfo.watchedMovies || [];

    return (
        <div className="profile-container">
            <div className="profile-header" style={{alignItems: 'flex-start', display: 'flex', gap: '20px', marginBottom: '30px'}}>
                <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                    <div style={{width: '100px', height: '100px', borderRadius: '50%', overflow: 'hidden', border: '2px solid #ccc'}}>
                        <img
                            src={profileInfo.avatarUrl || "https://via.placeholder.com/100"}
                            alt="avatar"
                            style={{width: '100%', height: '100%', objectFit: 'cover'}}
                        />
                    </div>
                    {!isNotMe ? (
                        <button className="nav-button" style={{marginTop: '10px', fontSize: '11px'}} onClick={() => setIsEditModalOpen(true)}>
                            ⚙️ Изменить профиль
                        </button>
                    ) : isAdmin && (
                        <button className="nav-button" style={{marginTop: '10px', fontSize: '11px', background: '#f44336', color: 'white'}} onClick={handleBlockUser}>
                            🚫 Заблокировать
                        </button>
                    )}
                </div>
                <div style={{paddingTop: '5px'}}>
                    <h2 style={{margin: '0 0 5px 0'}}>{profileInfo.username}</h2>
                    <p style={{color: '#666', margin: 0, fontSize: '14px'}}>{profileInfo.status || "Нет статуса"}</p>
                </div>
            </div>

            {isEditModalOpen && (
                <div className="modal-overlay" style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000}}>
                    <div className="profile-container" style={{width: '320px', background: 'white', padding: '20px', borderRadius: '15px'}}>
                        <h3 style={{marginTop: 0, textAlign: 'center'}}>Настройки</h3>
                        <label style={{fontSize: '12px', color: '#999'}}>Имя</label>
                        <input className="search-bar" style={{width: '100%', marginBottom: '10px'}}
                               value={editForm.username} onChange={e => setEditForm({...editForm, username: e.target.value})} />
                        <label style={{fontSize: '12px', color: '#999'}}>Статус</label>
                        <input className="search-bar" style={{width: '100%', marginBottom: '10px'}}
                               value={editForm.status} onChange={e => setEditForm({...editForm, status: e.target.value})} />
                        <label style={{fontSize: '12px', color: '#999'}}>URL Аватарки</label>
                        <input className="search-bar" style={{width: '100%', marginBottom: '20px'}}
                               value={editForm.avatarUrl} onChange={e => setEditForm({...editForm, avatarUrl: e.target.value})} />
                        <div style={{display: 'flex', gap: '10px'}}>
                            <button className="nav-button" style={{flex: 1, background: '#333', color: 'white'}} onClick={handleSaveProfile}>💾 Ок</button>
                            <button className="nav-button" style={{flex: 1}} onClick={() => setIsEditModalOpen(false)}>Отмена</button>
                        </div>
                    </div>
                </div>
            )}

            <div style={{background: '#f9f9f9', padding: '20px', borderRadius: '15px'}}>
                <h3 style={{textAlign: 'center'}}>Статистика {isNotMe ? profileInfo.username : 'моя'}</h3>

                <div className="pie-chart" style={{
                    margin: '0 auto 25px auto',
                    width: '180px',
                    height: '180px',
                    background: total > 0 ? `conic-gradient(#4caf50 0% ${watchedPercent}%, #ff9800 ${watchedPercent}% ${watchedPercent + deferredPercent}%, #f44336 ${watchedPercent + deferredPercent}% 100%)` : '#e0e0e0',
                    borderRadius: '50%'
                }}></div>

                <div style={{display: 'flex', gap: '25px', justifyContent: 'center', flexWrap: 'wrap'}}>
                    <p style={{margin: 0, fontWeight: '500'}}>
                        🟢 Просмотрено: {watchedCount} ({watchedPercent.toFixed(1)}%)
                    </p>
                    <p style={{margin: 0, fontWeight: '500'}}>
                        🟠 Отложено: {deferredCount} ({deferredPercent.toFixed(1)}%)
                    </p>
                    <p style={{margin: 0, fontWeight: '500'}}>
                        🔴 Брошено: {abandonedCount} ({abandonedPercent.toFixed(1)}%)
                    </p>
                </div>
            </div>

            <h3 style={{marginTop: '40px'}}>Недавние просмотренные</h3>
            <div className="recent-movies" style={{display: 'flex', gap: '15px', overflowX: 'auto', paddingBottom: '15px'}}>
                {watchedMovies.length > 0 ? watchedMovies.map(movie => (
                    <div key={movie.id}
                         className="recent-card"
                         style={{cursor: 'pointer', minWidth: '150px', maxWidth: '150px', height: '235px', background: 'white', borderRadius: '12px', padding: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', border: '1px solid #eee'}}
                         onClick={() => navigate(`/movie/${movie.id}`)}>
                        <div style={{height: '160px', background: '#ddd', borderRadius: '8px', overflow: 'hidden'}}>
                            {movie.imageUrl ? <img src={movie.imageUrl} style={{width:'100%', height:'100%', objectFit:'cover'}} alt="p" /> : <div style={{display:'flex', height:'100%', alignItems:'center', justifyContent:'center'}}>Постер</div>}
                        </div>
                        <p style={{fontSize: '13px', fontWeight: 'bold', margin: '10px 0 2px 0', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'}}>{movie.title}</p>
                        <p style={{fontSize: '11px', color: '#888', margin: 0}}>{movie.year}</p>
                    </div>
                )) : <p style={{fontSize: '14px', color: '#999', width: '100%', textAlign: 'center'}}>Здесь появятся ваши просмотренные фильмы</p>}
            </div>

            {!isNotMe && !isAdmin && (
                <div style={{marginTop: '50px', borderTop: '1px solid #eee', paddingTop: '20px'}}>
                    <button className="delete-account-btn" onClick={handleDeleteMyProfile}>
                        Удалить мой профиль
                    </button>
                </div>
            )}
        </div>
    );
}

const genreMap = {
    "Фантастика": ["Фантастика", "Sci-Fi", "Science Fiction"],
    "Драма": ["Драма", "Drama"],
    "Боевик": ["Боевик", "Action"],
    "Комедия": ["Комедия", "Comedy"],
    "Мистика": ["Мистика", "Mystery", "Mystic"]
};

function Genres({ isAdmin }) {
    const [genres, setGenres] = useState(() => {
        const saved = localStorage.getItem('my_app_genres');
        if (saved) {
            return JSON.parse(saved);
        }
        return Object.keys(genreMap);
    });
    const [movies, setMovies] = useState([]);
    const [isEditing, setIsEditing] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    const searchParams = new URLSearchParams(location.search);
    const searchQuery = searchParams.get('search')?.toLowerCase();

    useEffect(() => {
        localStorage.setItem('my_app_genres', JSON.stringify(genres));
    }, [genres]);

    useEffect(() => {
        movieService.searchMovies({ size: 100 })
            .then(res => setMovies(res.data.content || []))
            .catch(err => console.error("Ошибка загрузки", err));
    }, []);

    const addGenre = () => {
        const name = prompt("Введите название нового жанра:");
        if (name && name.trim() !== '' && !genres.includes(name.trim())) {
            setGenres([...genres, name.trim()]);
        }
    };
    const deleteGenre = (genreToDelete) => {
        setGenres(genres.filter(g => g !== genreToDelete));
    };

    return (
        <div className="genres-page">
            <div className="profile-container" style={{marginBottom: '30px', border: '1px solid #eee'}}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px'}}>
                    <h3 style={{margin: 0}}>Выбор жанра</h3>
                    {isAdmin && (
                        <div style={{display: 'flex', gap: '10px'}}>
                            {isEditing && <button className="nav-button" style={{border: '1px dashed #333', background: 'transparent'}} onClick={addGenre}>+ Добавить жанр</button>}
                            <button className="nav-button" onClick={() => setIsEditing(!isEditing)}>{isEditing ? "✅ Готово" : "⚙️ Редактировать"}</button>
                        </div>
                    )}
                </div>
                <div style={{display: 'flex', gap: '12px', flexWrap: 'wrap'}}>
                    {genres.map(g => (
                        <div key={g} style={{position: 'relative'}}>
                            <button className="nav-button" onClick={() => document.getElementById(g)?.scrollIntoView({behavior:'smooth'})}>{g}</button>
                            {isEditing && (
                                <button
                                    onClick={() => deleteGenre(g)}
                                    style={{position: 'absolute', top: '-6px', right: '-6px', borderRadius: '50%', background: 'red', color: 'white', border: 'none', width: '22px', height: '22px', fontSize: '12px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 5}}
                                >
                                    ×
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            </div>

            <div className="lists">
                {genres.map(genreName => {
                    const allowedTags = genreMap[genreName] || [genreName];
                    const filteredMovies = movies.filter(m => {
                        const matchesGenre = m.genres && m.genres.some(tag =>
                            allowedTags.some(allowed => allowed.toLowerCase() === tag.toLowerCase())
                        );
                        if (!matchesGenre) return false;
                        if (searchQuery) {
                            const matchTitle = m.title?.toLowerCase().includes(searchQuery);
                            const matchDirector = m.director?.toLowerCase().includes(searchQuery);
                            return matchTitle || matchDirector;
                        }
                        return true;
                    });

                    if (filteredMovies.length === 0 && !isEditing && !searchQuery) return null;
                    if (filteredMovies.length === 0 && searchQuery) return null;

                    return (
                        <div key={genreName} id={genreName} style={{marginBottom: '40px'}}>
                            <h3 style={{paddingLeft: '10px', borderLeft: '4px solid #333'}}>{genreName}</h3>
                            <div style={{display: 'flex', gap: '20px', overflowX: 'auto', padding: '10px 0'}}>
                                {filteredMovies.length > 0 ? filteredMovies.map(movie => (
                                    <div key={movie.id}
                                         className="movie-card"
                                         style={{minWidth: '200px', maxWidth: '200px', cursor: 'pointer'}}
                                         onClick={() => navigate(`/movie/${movie.id}`)}>
                                        <div style={{height: '280px', background: '#ccc', borderRadius: '10px', overflow: 'hidden', border: '1px solid #ddd'}}>
                                            {movie.imageUrl ? <img src={movie.imageUrl} style={{width:'100%', height:'100%', objectFit:'cover'}} alt="постер" /> : <div style={{display:'flex',height:'100%',alignItems:'center',justifyContent:'center', color:'#999'}}>Постер</div>}
                                        </div>
                                        <h4 style={{fontSize: '15px', margin: '10px 0 5px 0', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'}}>{movie.title}</h4>
                                        <p style={{fontSize: '13px', color: '#666', margin: '0 0 3px 0'}}>{movie.director}</p>
                                        <p style={{fontSize: '13px', color: '#999', margin: 0}}>{movie.year}</p>
                                    </div>
                                )) : <p style={{color: '#999', paddingLeft: '10px'}}>Фильмов нет</p>}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

function MovieDetail({ isAdmin, currentUser }) {
    const { id } = useParams();
    const [movie, setMovie] = useState(null);
    const [reviewContent, setReviewContent] = useState("");
    const [rating, setRating] = useState(5);
    const navigate = useNavigate();

    const [selectedUser, setSelectedUser] = useState(null);

    useEffect(() => {
        movieService.getById(id).then(res => setMovie(res.data));
    }, [id]);

    const handleAddReview = async () => {
        try {
            await movieService.addReview(id, {
                userId: currentUser.id,
                content: reviewContent,
                rating: rating
            });
            const res = await movieService.getById(id);
            setMovie(res.data);
            setReviewContent("");
        } catch (err) {
            console.error("Ошибка при добавлении отзыва:", err);
        }
    };

    const openMiniProfile = async (userId) => {
        try {
            const res = await movieService.getProfile(userId);
            setSelectedUser({ ...res.data, id: userId });
        } catch (err) { console.error("Ошибка"); }
    };

    const handleDeleteReview = (reviewId) => {
        movieService.deleteReview(reviewId).then(() => {
            movieService.getById(id).then(res => setMovie(res.data));
        });
    };

    const calculateAverage = () => {
        if (!movie?.reviews || movie.reviews.length === 0) return "0.0";
        const sum = movie.reviews.reduce((acc, r) => acc + r.rating, 0);
        return (sum / movie.reviews.length).toFixed(1);
    };

    if (!movie) return <div>Загрузка...</div>;

    return (
        <div className="movie-detail-container">
            {selectedUser && (
                <div className="modal-overlay" onClick={() => setSelectedUser(null)}>
                    <div className="mini-profile-card" onClick={e => e.stopPropagation()}>
                        <img src={selectedUser.avatarUrl || "https://via.placeholder.com/80"} className="mini-profile-avatar" alt="ava" />
                        <h3 style={{margin: '5px 0'}}>{selectedUser.username}</h3>
                        <p style={{fontSize: '12px', color: '#888'}}>{selectedUser.status}</p>
                        <div className="mini-stats-vertical" style={{ textAlign: 'left', margin: '15px 0', width: '100%' }}>
                            <div style={{ marginBottom: '8px' }}>
                                <span style={{ marginRight: '10px' }}>🟢</span>
                                <strong>Просмотрено:</strong> {selectedUser.statusChart?.WATCHED?.count || 0}
                            </div>
                            <div style={{ marginBottom: '8px' }}>
                                <span style={{ marginRight: '10px' }}>🟠</span>
                                <strong>Отложено:</strong> {selectedUser.statusChart?.DEFERRED?.count || 0}
                            </div>
                            <div style={{ marginBottom: '8px' }}>
                                <span style={{ marginRight: '10px' }}>🔴</span>
                                <strong>Брошено:</strong> {selectedUser.statusChart?.ABANDONED?.count || 0}
                            </div>
                        </div>
                        {isAdmin && selectedUser.id !== currentUser.id && (
                            <button
                                className="nav-button"
                                style={{width: '100%', marginTop: '15px', background: '#ff4d4d', color: 'white', border: 'none'}}
                                onClick={async () => {
                                    if (window.confirm(`Заблокировать ${selectedUser.username}?`)) {
                                        await movieService.deleteUser(selectedUser.id);
                                        setSelectedUser(null);
                                        alert("Пользователь удален");
                                    }
                                }}
                            >
                                🚫 Заблокировать
                            </button>
                        )}
                    </div>
                </div>
            )}

            <div className="movie-header-info">
                <div className="movie-poster-large">
                    {movie.imageUrl ? <img src={movie.imageUrl} style={{width: '100%'}} alt="постер" /> : "Нет фото"}
                </div>
                <div className="movie-info-text">
                    <div style={{display: 'flex', justifyContent: 'space-between'}}>
                        <h2>{movie.title}</h2>
                        <h2 style={{color: '#f39c12'}}>
                            ⭐ {calculateAverage()} / 5</h2>
                    </div>
                    <p><strong>Режиссёр:</strong> {movie.director}</p>
                    <p><strong>Год:</strong> {movie.year}</p>
                    <p><strong>Жанры:</strong> {movie.genres?.join(', ')}</p>

                    <div style={{marginTop: '20px'}}>
                        <label>Ваш статус: </label>
                        <select
                            className="status-select"
                            value={movie.status || 0}
                            onChange={(e) => {
                                const newStatus = Number(e.target.value);
                                movieService.updateProgress(id, newStatus)
                                    .then(() => {
                                        setMovie({
                                            ...movie,
                                            status: newStatus
                                        });
                                    })
                                    .catch(err => {
                                        console.error("Не удалось обновить статус", err);
                                        alert("Ошибка при сохранении статуса.");
                                    });
                            }}
                        >
                            <option value={0}>Не просмотрено</option>
                            <option value={1}>Просмотрено</option>
                            <option value={2}>Отложено</option>
                            <option value={3}>Брошено</option>
                        </select>
                    </div>
                </div>
            </div>

            <div className="video-player">
                <div style={{textAlign: 'center'}}>
                    <p>▶️ Плеер готов к воспроизведению фильма</p>
                    <p style={{fontSize: '12px', color: '#666'}}>{movie.title} ({movie.year})</p>
                </div>
            </div>

            <div className="reviews-section">
                <h3>Отзывы пользователей</h3>
                <div className="add-review-box">
        <textarea
            className="review-textarea"
            placeholder="Оставить отзыв..."
            value={reviewContent}
            onChange={e => setReviewContent(e.target.value)}
        />
                    <div className="review-actions">
                        <span className="rating-label">Оценка:</span>
                        <select
                            className="rating-select"
                            value={rating}
                            onChange={e => setRating(Number(e.target.value))}
                        >
                            <option value={0}>0</option>
                            <option value={1}>1</option>
                            <option value={2}>2</option>
                            <option value={3}>3</option>
                            <option value={4}>4</option>
                            <option value={5}>5</option>
                        </select>
                        <button className="submit-review-btn" onClick={handleAddReview}>
                            ✉️ Отправить
                        </button>
                    </div>
                </div>

                <div className="reviews-list">
                    {movie?.reviews?.map(review => (
                        <div key={review.id} className="review-item">
                            <img
                                src={review.avatarUrl || "https://via.placeholder.com/50"}
                                className="review-avatar"
                                onClick={() => openMiniProfile(review.userId)}
                                alt="ava"
                            />

                            <div className="review-content">
                                <div className="review-header">
                                    <strong
                                        className="review-author"
                                        onClick={() => openMiniProfile(review.userId)}
                                    >
                                        {review.username}
                                    </strong>
                                    <span className="review-stars">
                                        {"★".repeat(review.rating)}{"☆".repeat(5 - review.rating)}
                        </span>
                                </div>
                                <p className="review-text">{review.content}</p>
                            </div>

                            {(isAdmin || review.userId === currentUser?.id) && (
                                <button
                                    className="delete-review-btn"
                                    onClick={() => handleDeleteReview(review.id)}
                                    title="Удалить"
                                >
                                    ❌
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}