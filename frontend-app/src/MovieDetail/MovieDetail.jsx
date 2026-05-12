import { useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import '../App.css';
import {movieService} from "../api.jsx";

function MovieDetail ({ isAdmin, currentUser }) {
    const { id } = useParams();
    const [movie, setMovie] = useState(null);
    const [reviewContent, setReviewContent] = useState("");
    const [rating, setRating] = useState(5);

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
                        <h3 className="no-margin">{selectedUser.username}</h3>
                        <p className="profile-status-text">{selectedUser.status}</p>

                        <div className="mini-stats-list">
                            <div className="mini-stats-item"><span>🟢</span> <strong>Просмотрено:</strong> {selectedUser.statusChart?.WATCHED?.count || 0}</div>
                            <div className="mini-stats-item"><span>🟠</span> <strong>Отложено:</strong> {selectedUser.statusChart?.DEFERRED?.count || 0}</div>
                            <div className="mini-stats-item"><span>🔴</span> <strong>Брошено:</strong> {selectedUser.statusChart?.ABANDONED?.count || 0}</div>
                        </div>

                        {isAdmin && selectedUser.id !== currentUser?.id && (
                            <button className="nav-button delete full-width" onClick={async () => {
                                if (window.confirm(`Блокировать ${selectedUser.username}?`)) {
                                    await movieService.deleteUser(selectedUser.id);
                                    setSelectedUser(null);
                                }
                            }}>Заблокировать</button>
                        )}
                    </div>
                </div>
            )}

            <div className="movie-header-info">
                <div className="movie-poster-large">
                    <img src={movie.imageUrl || "https://ir.ozone.ru/s3/multimedia-1-e/7579533542.jpg"} alt="poster" />
                </div>
                <div className="movie-info-text">
                    <div className="movie-title-flex">
                        <h2>{movie.title}</h2>
                        <h2 className="movie-rating-badge">⭐ {calculateAverage()} / 5</h2>
                    </div>
                    <p><strong>Режиссёр:</strong> {movie.director}</p>
                    <p><strong>Год:</strong> {movie.year}</p>
                    <p><strong>Жанры:</strong> {movie.genres?.join(', ')}</p>

                    <div className="status-selector-box">
                        <label>Ваш статус: </label>
                        <select
                            className="status-select"
                            value={movie.status || 0}
                            onChange={(e) => {
                                const newStatus = Number(e.target.value);
                                movieService.updateProgress(id, newStatus)
                                    .then(() => setMovie({...movie, status: newStatus}))
                                    .catch(() => alert("Ошибка обновления статуса"));
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
                        <select className="rating-select" value={rating} onChange={e => setRating(Number(e.target.value))}>
                            {[0, 1, 2, 3, 4, 5].map(v => <option key={v} value={v}>{v}</option>)}
                        </select>
                        <button className="submit-review-btn" onClick={handleAddReview}>✉️ Отправить</button>
                    </div>
                </div>

                <div className="reviews-list">
                    {movie.reviews?.map(review => (
                        <div key={review.id} className="review-item">
                            <img
                                src={review.avatarUrl || "https://via.placeholder.com/50"}
                                className="review-avatar clickable"
                                onClick={() => openMiniProfile(review.userId)}
                                alt="ava"
                            />
                            <div className="review-content">
                                <div className="review-header">
                                    <strong className="review-author" onClick={() => openMiniProfile(review.userId)}>{review.username}</strong>
                                    <span className="review-stars">{"★".repeat(review.rating)}{"☆".repeat(5 - review.rating)}</span>
                                </div>
                                <p className="review-text">{review.content}</p>
                            </div>
                            {(isAdmin || review.userId === currentUser?.id) && (
                                <button className="delete-review-btn" onClick={() => handleDeleteReview(review.id)}>❌</button>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default MovieDetail;