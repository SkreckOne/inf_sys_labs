import React, { useState, useEffect, useMemo } from 'react';
import * as movieApi from '../api/movieApi'; // Импортируем все функции API
import MovieForm from './MovieForm';

const MovieList = () => {
    const [movies, setMovies] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [movieToEdit, setMovieToEdit] = useState(null);
    const [filter, setFilter] = useState('');
    const [filterColumn, setFilterColumn] = useState('name');
    const [sortConfig, setSortConfig] = useState({ key: 'id', direction: 'ascending' });

    const [deleteGenre, setDeleteGenre] = useState('DRAMA');
    const [tagline, setTagline] = useState('');
    const [fromGenre, setFromGenre] = useState('DRAMA');
    const [toGenre, setToGenre] = useState('COMEDY');

    const genres = ['DRAMA', 'COMEDY', 'MUSICAL', 'ADVENTURE', 'SCIENCE_FICTION'];


    const fetchMovies = async () => {
        try {
            const response = await movieApi.getMovies();
            setMovies(response.data);
            console.log("Data fetched and table updated!");
        } catch (error) {
            console.error("Failed to fetch movies", error);
        }
    };

    useEffect(() => {
        fetchMovies();

        const eventSource = new EventSource('/api/sse/subscribe');
        eventSource.onopen = () => console.log("SSE Connection Opened!");
        eventSource.onerror = (err) => console.error("SSE Error:", err);


        const handleEvent = (event) => {
            console.log("Received SSE event:", event.type);
            fetchMovies();
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
    }, []);

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


    const requestSort = (key) => {
        let direction = 'ascending';
        if (sortConfig.key === key && sortConfig.direction === 'ascending') {
            direction = 'descending';
        }
        setSortConfig({ key, direction });
    };

    const getSortIndicator = (key) => {
        if (sortConfig.key !== key) return null;
        return sortConfig.direction === 'ascending' ? ' ▲' : ' ▼';
    };

    const processedMovies = useMemo(() => {
        let processableMovies = [...movies];
        if (filter) {
            processableMovies = processableMovies.filter(movie => {
                const valueToFilter = filterColumn === 'director' ? movie.director?.name : movie[filterColumn];
                return valueToFilter?.toString().toLowerCase() === filter.toLowerCase();
            });
        }
        if (sortConfig.key !== null) {
            processableMovies.sort((a, b) => {
                const valA = sortConfig.key === 'director' ? a.director?.name : a[sortConfig.key];
                const valB = sortConfig.key === 'director' ? b.director?.name : b[sortConfig.key];
                if (valA == null) return 1; if (valB == null) return -1;
                if (valA < valB) return sortConfig.direction === 'ascending' ? -1 : 1;
                if (valA > valB) return sortConfig.direction === 'ascending' ? 1 : -1;
                return 0;
            });
        }
        return processableMovies;
    }, [movies, filter, filterColumn, sortConfig]);


    return (
        <>
            <div className="container">
                <h1>Movie Information System</h1>
                <div className="controls-container">
                    <div className="filter-container">
                        <select value={filterColumn} onChange={e => setFilterColumn(e.target.value)}>
                            <option value="name">Name</option>
                            <option value="genre">Genre</option>
                            <option value="director">Director</option>
                            <option value="tagline">Tagline</option>
                        </select>
                        <input
                            type="text"
                            placeholder="Filter by exact match (case-insensitive)..."
                            value={filter}
                            onChange={(e) => setFilter(e.target.value)}
                        />
                    </div>
                    <button className="button button-primary" onClick={handleAddMovie}>Add New Movie</button>
                </div>

                <table>
                    <thead>
                    <tr>
                        <th onClick={() => requestSort('id')}>ID{getSortIndicator('id')}</th>
                        <th onClick={() => requestSort('name')}>Name{getSortIndicator('name')}</th>
                        <th onClick={() => requestSort('genre')}>Genre{getSortIndicator('genre')}</th>
                        <th onClick={() => requestSort('director')}>Director{getSortIndicator('director')}</th>
                        <th onClick={() => requestSort('oscarsCount')}>Oscars{getSortIndicator('oscarsCount')}</th>
                        <th onClick={() => requestSort('budget')}>Budget{getSortIndicator('budget')}</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {processedMovies.map((movie) => (
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