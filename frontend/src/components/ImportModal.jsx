import React, { useState } from 'react';
import { importMovies } from '../api/movieApi';

const ImportModal = ({ onClose, onImportSuccess }) => {
    const [file, setFile] = useState(null);
    const [isUploading, setIsUploading] = useState(false);
    const [error, setError] = useState('');

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file) {
            setError('Please select a JSON file to import.');
            return;
        }

        setIsUploading(true);
        setError('');
        try {
            await importMovies(file);
            alert('File import successful!');
            onImportSuccess();
            onClose();
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'An unexpected error occurred.';
            setError(errorMessage);
            console.error('Import failed:', err);
        } finally {
            setIsUploading(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h2>Import Movies from JSON</h2>
                    <button onClick={onClose} className="modal-close-button">&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>JSON File</label>
                        <input type="file" accept=".json" onChange={handleFileChange} />
                    </div>
                    {error && <p style={{ color: 'red' }}>{error}</p>}
                    <div className="form-actions">
                        <button type="button" className="button" onClick={onClose} disabled={isUploading}>
                            Cancel
                        </button>
                        <button type="submit" className="button button-primary" disabled={isUploading}>
                            {isUploading ? 'Uploading...' : 'Import'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ImportModal;