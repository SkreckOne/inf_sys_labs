import React, { useState, useEffect, useCallback } from 'react';
import * as movieApi from '../api/movieApi';
import MovieForm from './MovieForm';

const MovieList = () => {
    const [movies, setMovies] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [movieToEdit, setMovieToEdit] = useState(null);

    const [pagination, setPagination] = useState({
        page: 0,
        size: 10,
        totalPages: 0,
    });

    // --- НОВЫЙ БЛОК: Состояние для фильтрации и сортировки ---
    const [filters, setFilters] = useState({
        name: '',
        genre: '',
        directorName: '',
    });
    const [sort, setSort] = useState({ key: 'id', order: 'asc' });


    const [deleteGenre, setDeleteGenre] = useState('DRAMA');
    const [tagline, setTagline] = useState('');
    const [fromGenre, setFromGenre] = useState('DRAMA');
    const [toGenre, setToGenre] = useState('COMEDY');

    const genres = ['DRAMA', 'COMEDY', 'MUSICAL', 'ADVENTURE', 'SCIENCE_FICTION'];

    // --- Обновленная функция для запроса данных ---
    const fetchMovies = useCallback(async () => {
        try {
            const response = await movieApi.getMovies(pagination.page, pagination.size, filters, sort);
            setMovies(response.data.content);
            setPagination(prev => ({ ...prev, totalPages: response.data.totalPages }));
            console.log(`Data fetched for page: ${pagination.page}, sort: ${sort.key}, filters:`, filters);
        } catch (error) {
            console.error("Failed to fetch movies", error);
        }
    }, [pagination.page, pagination.size, filters, sort]);

    useEffect(() => {
        fetchMovies();

        const eventSource = new EventSource('/api/sse/subscribe');
        eventSource.onopen = () => console.log("SSE Connection Opened!");
        eventSource.onerror = (err) => console.error("SSE Error:", err);

        const handleEvent = (event) => {
            console.log("Received SSE event:", event.type);
            fetchMovies(); // Перезагружаем данные с учетом текущих фильтров/сортировки
        };

        eventSource.addEventListener('movie-created', handleEvent);
        eventSource.addEventListener('movie-updated', handleEvent);
        eventSource.addEventListener('movie-deleted', handleEvent);
        eventSource.addEventListener('movies-deleted-by-genre', handleEvent);
        eventSource.addEventListener('oscars-redistributed', handleEvent);

        return () => {
            console.log("Closing SSE connection.");
            eventSource.close();
        };
    }, [fetchMovies]);

    // --- НОВЫЙ БЛОК: Обработчики для UI ---
    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const handleSort = (key) => {
        setSort(prev => {
            if (prev.key === key) {
                return { key, order: prev.order === 'asc' ? 'desc' : 'asc' };
            }
            return { key, order: 'asc' };
        });
    };

    const getSortIndicator = (key) => {
        if (sort.key === key) {
            return sort.order === 'asc' ? ' ▲' : ' ▼';
        }
        return '';
    };

    const handleAddMovie = () => {
        setMovieToEdit(null);
        setIsModalOpen(true);
    };

    const handleEditMovie = (movie) => {
        setMovieToEdit(movie);
        setIsModalOpen(true);
    };

    const handleDeleteMovie = async (id) => {
        if (window.confirm('Are you sure you want to delete this movie?')) {
            try {
                await movieApi.deleteMovie(id);
            } catch(error){
                console.error("Failed to delete movie", error);
            }
        }
    };

    const handleFormSubmit = () => {
        setIsModalOpen(false);
    };

    const handleGetSum = async () => {
        try {
            const response = await movieApi.getGoldenPalmSum();
            alert(`Total Golden Palms: ${response.data.totalGoldenPalms}`);
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    };

    const handleFindTagline = async () => {
        if (!tagline) return alert('Please enter a tagline substring.');
        try {
            const response = await movieApi.findMoviesByTagline(tagline);
            const movieNames = response.data.map(m => m.name).join(',\n');
            alert(`Movies found:\n${movieNames || 'None'}`);
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    };

    const handleGetScreenwriters = async () => {
        try {
            const response = await movieApi.getScreenwritersWithoutOscars();
            const names = response.data.map(p => p.name).join(',\n');
            alert(`Screenwriters with no Oscars:\n${names || 'None'}`);
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    };

    const handleDeleteByGenre = async () => {
        if (window.confirm(`Are you sure you want to delete all movies with genre ${deleteGenre}?`)) {
            try {
                await movieApi.deleteMoviesByGenre(deleteGenre);
            } catch (error) {
                alert(`Error: ${error.message}`);
            }
        }
    };

    const handleRedistribute = async () => {
        if (window.confirm(`Are you sure you want to redistribute Oscars from ${fromGenre} to ${toGenre}?`)) {
            try {
                await movieApi.redistributeOscars(fromGenre, toGenre);
            } catch (error) {
                alert(`Error: ${error.message}`);
            }
        }
    };

    const handleNextPage = () => {
        if (pagination.page < pagination.totalPages - 1) {
            setPagination(prev => ({ ...prev, page: prev.page + 1 }));
        }
    };

    const handlePrevPage = () => {
        if (pagination.page > 0) {
            setPagination(prev => ({ ...prev, page: prev.page - 1 }));
        }
    };

    return (
        <>
            <div className="container">
                <h1>Movie Information System</h1>
                <div className="controls-container">
                    {/* --- НОВЫЙ БЛОК: Фильтры --- */}
                    <div className="filter-container">
                        <input type="text" name="name" placeholder="Filter by Name (exact match)" value={filters.name} onChange={handleFilterChange} />
                        <input type="text" name="directorName" placeholder="Filter by Director (exact match)" value={filters.directorName} onChange={handleFilterChange} />
                        <select name="genre" value={filters.genre} onChange={handleFilterChange}>
                            <option value="">All Genres</option>
                            {genres.map(g => <option key={g} value={g}>{g}</option>)}
                        </select>
                    </div>
                    <button className="button button-primary" onClick={handleAddMovie}>Add New Movie</button>
                </div>

                <table>
                    <thead>
                    <tr>
                        {/* --- Обновленные заголовки для сортировки --- */}
                        <th onClick={() => handleSort('id')}>ID{getSortIndicator('id')}</th>
                        <th onClick={() => handleSort('name')}>Name{getSortIndicator('name')}</th>
                        <th onClick={() => handleSort('genre')}>Genre{getSortIndicator('genre')}</th>
                        <th onClick={() => handleSort('director.name')}>Director{getSortIndicator('director.name')}</th>
                        <th onClick={() => handleSort('oscarsCount')}>Oscars{getSortIndicator('oscarsCount')}</th>
                        <th onClick={() => handleSort('budget')}>Budget{getSortIndicator('budget')}</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {movies.map((movie) => (
                        <tr key={movie.id}>
                            <td>{movie.id}</td>
                            <td>{movie.name}</td>
                            <td>{movie.genre}</td>
                            <td>{movie.director?.name || 'N/A'}</td>
                            <td>{movie.oscarsCount}</td>
                            <td>${movie.budget.toLocaleString()}</td>
                            <td>
                                <button className="button button-secondary" onClick={() => handleEditMovie(movie)}>Edit</button>
                                <button className="button button-danger" onClick={() => handleDeleteMovie(movie.id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                <div className="pagination-controls">
                    <button onClick={handlePrevPage} disabled={pagination.page === 0} className="button">
                        Previous
                    </button>
                    <span>
                        Page {pagination.page + 1} of {pagination.totalPages || 1}
                    </span>
                    <button onClick={handleNextPage} disabled={pagination.page >= pagination.totalPages - 1} className="button">
                        Next
                    </button>
                </div>

                {isModalOpen && (
                    <MovieForm
                        movieToEdit={movieToEdit}
                        onFormSubmit={handleFormSubmit}
                        onCancel={() => setIsModalOpen(false)}
                    />
                )}
            </div>

            <div className="special-ops-container">
                <h2>Special Operations</h2>
                <div className="ops-grid">
                    <div className="op-item">
                        <button onClick={handleGetSum}>Sum Golden Palms</button>
                    </div>
                    <div className="op-item">
                        <button onClick={handleGetScreenwriters}>Screenwriters w/o Oscars</button>
                    </div>
                    <div className="op-item">
                        <input
                            type="text"
                            placeholder="Tagline contains..."
                            value={tagline}
                            onChange={e => setTagline(e.target.value)}
                        />
                        <button onClick={handleFindTagline}>Find</button>
                    </div>
                    <div className="op-item">
                        <select value={deleteGenre} onChange={e => setDeleteGenre(e.target.value)}>
                            {genres.map(g => <option key={g} value={g}>{g}</option>)}
                        </select>
                        <button onClick={handleDeleteByGenre} className="button-danger">Delete by Genre</button>
                    </div>
                    <div className="op-item">
                        <label>From:</label>
                        <select value={fromGenre} onChange={e => setFromGenre(e.target.value)}>
                            {genres.map(g => <option key={`f-${g}`} value={g}>{g}</option>)}
                        </select>
                        <label>To:</label>
                        <select value={toGenre} onChange={e => setToGenre(e.target.value)}>
                            {genres.map(g => <option key={`t-${g}`} value={g}>{g}</option>)}
                        </select>
                        <button onClick={handleRedistribute} className="button-secondary">Redistribute Oscars</button>
                    </div>
                </div>
            </div>
        </>
    );
};

export default MovieList;