import React, { useState, useEffect } from 'react';
import { createMovie, updateMovie } from '../api/movieApi';

const MovieForm = ({ movieToEdit, onFormSubmit, onCancel }) => {
    const getInitialState = () => ({
        name: '',
        oscarsCount: '',
        budget: '',
        totalBoxOffice: '',
        mpaaRating: 'G',
        length: '',
        goldenPalmCount: '',
        usaBoxOffice: '',
        tagline: '',
        genre: 'DRAMA',
        coordinates: { x: '', y: '' },
        director: { name: '', eyeColor: 'GREEN' },
        screenwriter: { name: '', eyeColor: 'GREEN' },
        operator: { name: '', eyeColor: 'GREEN' },
    });

    const [formData, setFormData] = useState(getInitialState());

    useEffect(() => {
        if (movieToEdit) {
            const formattedData = getInitialState();

            Object.keys(movieToEdit).forEach(key => {
                const value = movieToEdit[key];
                if (value === null) {
                } else if (typeof value === 'object' && value !== null) {
                    formattedData[key] = { ...formattedData[key], ...value };
                } else {
                    formattedData[key] = value;
                }
            });

            setFormData({ ...formattedData, id: movieToEdit.id });
        } else {
            setFormData(getInitialState());
        }
    }, [movieToEdit]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleNestedChange = (parent, e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [parent]: { ...prev[parent], [name]: value },
        }));
    };

    const prepareDataForSubmit = (data) => {
        const payload = JSON.parse(JSON.stringify(data));

        const numericFields = ['oscarsCount', 'length', 'usaBoxOffice'];
        numericFields.forEach(field => {
            payload[field] = payload[field] === '' ? null : Number(payload[field]);
        });

        payload.budget = Number(payload.budget);
        payload.totalBoxOffice = Number(payload.totalBoxOffice);
        payload.goldenPalmCount = Number(payload.goldenPalmCount);

        if (payload.coordinates) {
            payload.coordinates.x = Number(payload.coordinates.x);
            payload.coordinates.y = Number(payload.coordinates.y);
        }

        if (payload.screenwriter && payload.screenwriter.name.trim() === '') {
            payload.screenwriter = null;
        }

        return payload;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const dataToSend = prepareDataForSubmit(formData);

        try {
            if (dataToSend.id) {
                await updateMovie(dataToSend.id, dataToSend);
            } else {
                await createMovie(dataToSend);
            }
            onFormSubmit();
        } catch (error) {
            console.error("Failed to submit form", error);
            const errorData = error.response?.data?.validation_errors;
            if (errorData) {
                const errorMessages = Object.entries(errorData).map(([field, message]) => `${field}: ${message}`).join('\n');
                alert("Validation Error:\n" + errorMessages);
            } else {
                alert("An unknown error occurred: " + (error.response?.data?.error || error.message));
            }
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h2>{formData.id ? 'Edit Movie' : 'Add New Movie'}</h2>
                    <button onClick={onCancel} className="modal-close-button">&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    {/* Форма остается без изменений */}
                    <div className="form-grid">
                        <div className="form-group">
                            <label>Name*</label>
                            <input name="name" value={formData.name} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label>Tagline*</label>
                            <input name="tagline" value={formData.tagline} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label>Genre</label>
                            <select name="genre" value={formData.genre} onChange={handleChange}>
                                <option value="DRAMA">Drama</option>
                                <option value="COMEDY">Comedy</option>
                                <option value="MUSICAL">Musical</option>
                                <option value="ADVENTURE">Adventure</option>
                                <option value="SCIENCE_FICTION">Science Fiction</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label>MPAA Rating*</label>
                            <select name="mpaaRating" value={formData.mpaaRating} onChange={handleChange} required>
                                <option value="G">G</option>
                                <option value="PG">PG</option>
                                <option value="PG_13">PG-13</option>
                                <option value="NC_17">NC-17</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Budget*</label>
                            <input type="number" name="budget" value={formData.budget} onChange={handleChange} required min="1"/>
                        </div>
                        <div className="form-group">
                            <label>Total Box Office*</label>
                            <input type="number" name="totalBoxOffice" value={formData.totalBoxOffice} onChange={handleChange} required min="1"/>
                        </div>
                        <div className="form-group">
                            <label>USA Box Office</label>
                            <input type="number" name="usaBoxOffice" value={formData.usaBoxOffice} onChange={handleChange} min="1"/>
                        </div>
                        <div className="form-group">
                            <label>Oscars</label>
                            <input type="number" name="oscarsCount" value={formData.oscarsCount} onChange={handleChange} min="1"/>
                        </div>
                        <div className="form-group">
                            <label>Golden Palms*</label>
                            <input type="number" name="goldenPalmCount" value={formData.goldenPalmCount} onChange={handleChange} required min="1"/>
                        </div>
                        <div className="form-group">
                            <label>Length (min)</label>
                            <input type="number" name="length" value={formData.length} onChange={handleChange} min="1"/>
                        </div>
                        <div className="form-group">
                            <label>Coordinate X*</label>
                            <input type="number" name="x" value={formData.coordinates.x} onChange={(e) => handleNestedChange('coordinates', e)} required max="506"/>
                        </div>
                        <div className="form-group">
                            <label>Coordinate Y*</label>
                            <input type="number" name="y" value={formData.coordinates.y} onChange={(e) => handleNestedChange('coordinates', e)} required />
                        </div>
                        <div className="form-group">
                            <label>Director*</label>
                            <input name="name" value={formData.director.name} onChange={(e) => handleNestedChange('director', e)} required />
                        </div>
                        <div className="form-group">
                            <label>Screenwriter</label>
                            <input name="name" value={formData.screenwriter.name} onChange={(e) => handleNestedChange('screenwriter', e)} />
                        </div>
                        <div className="form-group">
                            <label>Operator*</label>
                            <input name="name" value={formData.operator.name} onChange={(e) => handleNestedChange('operator', e)} required />
                        </div>
                    </div>
                    <div className="form-actions">
                        <button type="button" className="button" onClick={onCancel}>Cancel</button>
                        <button type="submit" className="button button-primary">{formData.id ? 'Update Movie' : 'Create Movie'}</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default MovieForm;