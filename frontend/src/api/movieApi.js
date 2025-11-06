import axios from 'axios';

const MOVIES_API_URL = '/api/movies';
const OPS_API_URL = '/api/operations';

export const getMovies = (page = 0, size = 10, filters = {}, sort = { key: 'id', order: 'asc' }) => {
    let params = `page=${page}&size=${size}&sort=${sort.key},${sort.order}`;

    Object.keys(filters).forEach(key => {
        if (filters[key]) {
            params += `&${key}=${encodeURIComponent(filters[key])}`;
        }
    });

    return axios.get(`${MOVIES_API_URL}?${params}`);
};
export const importMovies = (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return axios.post('/api/import', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
};
export const getMovieById = (id) => axios.get(`${MOVIES_API_URL}/${id}`);
export const createMovie = (movie) => axios.post(MOVIES_API_URL, movie);
export const updateMovie = (id, movie) => axios.put(`${MOVIES_API_URL}/${id}`, movie);
export const deleteMovie = (id) => axios.delete(`${MOVIES_API_URL}/${id}`);

export const deleteMoviesByGenre = (genre) => axios.delete(`${OPS_API_URL}/genre/${genre}`);
export const getGoldenPalmSum = () => axios.get(`${OPS_API_URL}/golden-palm-sum`);
export const findMoviesByTagline = (substring) => axios.get(`${OPS_API_URL}/tagline?contains=${substring}`);
export const getScreenwritersWithoutOscars = () => axios.get(`${OPS_API_URL}/screenwriters-no-oscars`);
export const redistributeOscars = (fromGenre, toGenre) => axios.post(`${OPS_API_URL}/redistribute-oscars?from=${fromGenre}&to=${toGenre}`);
export const getImportHistory = () => axios.get('/api/import/history');