import React, { useState, useEffect } from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import {movieService} from "../api.jsx";
import '../App.css';

function Home ({ isAdmin, title }) {
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

                const getMovies = (res) => (res.data?.content || (Array.isArray(res.data) ? res.data : []));
                const combined = [...getMovies(resT), ...getMovies(resD)];
                let results = Array.from(new Map(combined.map(m => [m.id, m])).values());
                if (title === "Новинки") results.sort((a, b) => b.year - a.year);
                if (title === "Популярное") results.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
                setMovies(results);
                setTotalElements(results.length);
            } else {
                let res = await movieService.searchMovies({ size: pageSize });
                let list = res.data?.content || (Array.isArray(res.data) ? res.data : []);
                setTotalElements(res.data.totalElements || list.length);
                if (title === "Новинки") list.sort((a, b) => b.year - a.year);
                if (title === "Популярное") list.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
                setMovies(list);

            }
        } catch (err) {
            console.error("Ошибка загрузки:", err);
        }
    };

    useEffect(() => {
        fetchMovies();
    }, [title, pageSize, location.search]);

    const handleDelete = async (e, id) => {
        e.stopPropagation();
        if (window.confirm("Вы уверены, что хотите удалить этот фильм?")) {
            await movieService.delete(id);
            fetchMovies();
        }
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
            <div className="home-header">
                <h2>{title} {location.search && <span className="search-results-tag">— Поиск</span>}</h2>
                {isAdmin && <button className="nav-button" onClick={() => setIsModalOpen(true)}>➕ Создать</button>}
            </div>

            {isModalOpen && (
                <div className="modal-overlay">
                    <form className="modal-content" onSubmit={handleCreate}>
                        <h3>Новый фильм</h3>
                        <input type="text" placeholder="Название" className="search-bar" value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} />
                        <input type="number" placeholder="Год" className="search-bar" value={formData.year} onChange={e => setFormData({...formData, year: e.target.value})} />
                        <input type="text" placeholder="Режиссер" className="search-bar" value={formData.director} onChange={e => setFormData({...formData, director: e.target.value})} />
                        <input type="text" placeholder="Жанры" className="search-bar" value={formData.genres} onChange={e => setFormData({...formData, genres: e.target.value})} />
                        <input type="text" placeholder="URL постера" className="search-bar" value={formData.imageUrl} onChange={e => setFormData({...formData, imageUrl: e.target.value})} />
                        <div style={{display: 'flex', gap: '10px'}}>
                            <button type="submit" className="nav-button active" style={{flex: 1}}>Создать</button>
                            <button type="button" className="nav-button" style={{flex: 1}} onClick={() => setIsModalOpen(false)}>Отмена</button>
                        </div>
                    </form>
                </div>
            )}

            {editingMovie && (
                <div className="modal-overlay">
                    <form className="modal-content" onSubmit={handleEditSubmit}>
                        <h3>Редактировать</h3>
                        <div className="form-group">
                            <span className="form-label">Название:</span>
                            <input type="text" className="search-bar" value={editingMovie.title} onChange={e => setEditingMovie({...editingMovie, title: e.target.value})} />
                        </div>
                        <div className="form-group">
                            <span className="form-label">Год:</span>
                            <input type="number" className="search-bar" value={editingMovie.year} onChange={e => setEditingMovie({...editingMovie, year: e.target.value})} />
                        </div>
                        <div className="form-group">
                            <span className="form-label">Автор:</span>
                            <input type="text" className="search-bar" value={editingMovie.director} onChange={e => setEditingMovie({...editingMovie, director: e.target.value})} />
                        </div>
                        <div className="form-group">
                            <span className="form-label">Жанры:</span>
                            <input type="text" className="search-bar" value={editingMovie.genres} onChange={e => setEditingMovie({...editingMovie, genres: e.target.value})} />
                        </div>
                        <div className="form-group">
                            <span className="form-label">Постер:</span>
                            <input type="text" className="search-bar" value={editingMovie.imageUrl} onChange={e => setEditingMovie({...editingMovie, imageUrl: e.target.value})} />
                        </div>
                        <div style={{display: 'flex', gap: '10px'}}>
                            <button type="submit" className="nav-button active" style={{flex: 1}}>Сохранить</button>
                            <button type="button" className="nav-button" style={{flex: 1}} onClick={() => setEditingMovie(null)}>Отмена</button>
                        </div>
                    </form>
                </div>
            )}

            <div className="movie-grid">
                {movies.map(movie => (
                    <div key={movie.id} className="movie-card" onClick={() => navigate(`/movie/${movie.id}`)}>
                        {isAdmin && (
                            <div className="card-actions">
                                <button className="action-btn" onClick={(e) => { e.stopPropagation(); setEditingMovie({...movie, genres: movie.genres?.join(', ')}); }}>✏️</button>
                                <button className="action-btn delete" onClick={(e) => handleDelete(e, movie.id)}>🗑️</button>
                            </div>
                        )}
                        <div className="movie-poster-container">
                            <img
                                src={movie.imageUrl || "https://ir.ozone.ru/s3/multimedia-1-e/7579533542.jpg"}
                                className="movie-poster-img"
                                alt="постер"
                            />
                        </div>
                        <div className="movie-info">
                            <h3 style={{fontSize: '18px', margin: '0 0 10px 0'}}>{movie.title}</h3>
                            <h3 style={{fontSize: '18px', margin: '0 0 10px 0'}}>{movie.director}</h3>
                            <h3 style={{fontSize: '18px', margin: '0 0 10px 0'}}>{movie.year}</h3>
                            <p style={{fontSize: '14px', color: '#666'}}>{movie.genres?.join(', ')}</p>
                            <div className="movie-rating">
                                <span style={{color: '#f39c12'}}>⭐</span>
                                <b>{movie.averageRating?.toFixed(1) || "0.0"}</b>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {movies.length < totalElements && !location.search && (
                <div className="load-more-container">
                    <button className="nav-button active" onClick={() => setPageSize(prev => prev + 10)}>Показать еще</button>
                </div>
            )}
        </div>
    );
}

export default Home;