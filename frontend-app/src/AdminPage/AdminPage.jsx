import React, {useEffect, useState} from "react";
import {movieService} from "../api.jsx";
import '../App.css';

function AdminPage() {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [viewMode, setViewMode] = useState('schema');

    useEffect(() => {
        if (viewMode === 'table' && data.length === 0) {
            setLoading(true);
            movieService.searchMovies({ size: 50 })
                .then(res => setData(res.data.content || []))
                .finally(() => setLoading(false));
        }
    }, [viewMode]);

    return (
        <div className="admin-container">
            <div className="admin-header">
                <h1>Панель администратора</h1>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className={`admin-view-btn ${viewMode === 'schema' ? 'active-schema' : ''}`}
                        onClick={() => setViewMode(viewMode === 'schema' ? 'table' : 'schema')}
                    >
                        {viewMode === 'schema' ? 'Показать таблицы' : 'Показать связи'}
                    </button>
                </div>
            </div>

            {viewMode === 'schema' ? (
                <div className="schema-wrapper">
                    <div className="schema-section">
                        <h3 style={{ textAlign: 'center', marginBottom: '20px' }}>One-to-Many</h3>
                        <div className="schema-list">
                            <div className="relation-item">
                                <div className="entity-block">Movie</div><span className="arrow-icon">➡️</span><div className="entity-block">Reviews</div>
                            </div>
                            <div className="relation-item">
                                <div className="entity-block">Director</div><span className="arrow-icon">➡️</span><div className="entity-block">Movies</div>
                            </div>
                            <div className="relation-item">
                                <div className="entity-block">User</div><span className="arrow-icon">⬅️</span><div className="entity-block">Reviews</div>
                            </div>
                        </div>
                    </div>

                    <div className="schema-section" style={{ textAlign: 'center' }}>
                        <h3 style={{ marginBottom: '20px' }}>Many-to-Many</h3>
                        <div className="relation-item" style={{ justifyContent: 'center' }}>
                            <div className="entity-block">Genres</div><span className="arrow-icon">↔️</span><div className="entity-block">Movies</div>
                        </div>
                    </div>
                </div>
            ) : (
                <div className="admin-tables-list">
                    {loading ? <p>Загрузка данных из БД...</p> : (
                        <>
                            <section>
                                <h3>Данные: Many-to-Many (Фильмы и Жанры)</h3>
                                <table className="admin-table">
                                    <thead>
                                    <tr><th>Фильм</th><th>Жанры</th></tr>
                                    </thead>
                                    <tbody>
                                    {data.map(m => (
                                        <tr key={m.id}>
                                            <td><strong>{m.title}</strong></td>
                                            <td>{m.genres?.join(', ')}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </section>

                            <section>
                                <h3>Данные: One-to-Many (Фильмы и Отзывы)</h3>
                                <table className="admin-table">
                                    <thead>
                                    <tr><th>Фильм</th><th>Отзывы</th></tr>
                                    </thead>
                                    <tbody>
                                    {data.map(m => (
                                        <tr key={m.id}>
                                            <td>{m.title}</td>
                                            <td align="center">{m.reviews?.length || 0}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </section>
                        </>
                    )}
                </div>
            )}
        </div>
    );
}

export default AdminPage;