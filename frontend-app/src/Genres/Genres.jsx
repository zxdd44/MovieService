import React, {useEffect, useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import {movieService} from "../api.jsx";
import '../App.css';

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
        return saved ? JSON.parse(saved) : Object.keys(genreMap);
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

    return (
        <div className="genres-page" style={{ padding: '20px' }}>
            <div className="profile-container" style={{ marginBottom: '30px', border: '1px solid #eee', padding: '20px', borderRadius: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                    <h3 style={{ margin: 0 }}>Выбор жанра</h3>
                    {isAdmin && (
                        <div style={{ display: 'flex', gap: '10px' }}>
                            {isEditing && <button className="nav-button" onClick={addGenre}>+ Добавить</button>}
                            <button className="nav-button" onClick={() => setIsEditing(!isEditing)}>
                                {isEditing ? "✅ Готово" : "⚙️ Редактировать"}
                            </button>
                        </div>
                    )}
                </div>
                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                    {genres.map(g => (
                        <div key={g} style={{ position: 'relative' }}>
                            <button className="nav-button" onClick={() => document.getElementById(g)?.scrollIntoView({ behavior: 'smooth' })}>{g}</button>
                            {isEditing && (
                                <button
                                    onClick={() => setGenres(genres.filter(item => item !== g))}
                                    style={{ position: 'absolute', top: '-6px', right: '-6px', borderRadius: '50%', background: 'red', color: 'white', border: 'none', width: '22px', height: '22px', cursor: 'pointer' }}
                                >×</button>
                            )}
                        </div>
                    ))}
                </div>
            </div>
            <div className="lists">
                {genres.map(genreName => {
                    const allowedTags = genreMap[genreName] || [genreName];
                    const filteredMovies = movies.filter(m => {
                        const matchesGenre = m.genres?.some(tag =>
                            allowedTags.some(allowed => allowed.toLowerCase() === tag.toLowerCase())
                        );
                        if (!matchesGenre) return false;
                        return searchQuery ? (m.title?.toLowerCase().includes(searchQuery) || m.director?.toLowerCase().includes(searchQuery)) : true;
                    });

                    if (filteredMovies.length === 0 && !isEditing && !searchQuery) return null;

                    return (
                        <div key={genreName} id={genreName} style={{ marginBottom: '40px' }}>
                            <h3 style={{ paddingLeft: '10px', borderLeft: '4px solid #333' }}>{genreName}</h3>
                            <div style={{ display: 'flex', gap: '20px', overflowX: 'auto', padding: '10px 0' }}>
                                {filteredMovies.length > 0 ? filteredMovies.map(movie => (
                                    <div key={movie.id} className="movie-card" style={{ minWidth: '200px', maxWidth: '200px', cursor: 'pointer' }} onClick={() => navigate(`/movie/${movie.id}`)}>
                                        <div className="movie-poster-container" style={{ height: '280px' }}>
                                            {movie.imageUrl ? <img src={movie.imageUrl} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '10px' }} alt="p" /> : <div className="placeholder"></div>}
                                        </div>
                                        <h4 style={{ fontSize: '15px', margin: '10px 0 5px 0' }}>{movie.title}</h4>
                                        <p style={{ fontSize: '13px', color: '#666', margin: 0 }}>{movie.director}, {movie.year}</p>
                                    </div>
                                )) : <p style={{ color: '#999' }}>Пусто</p>}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

export default Genres;