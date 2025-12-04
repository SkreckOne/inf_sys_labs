import React, { useState, useEffect } from 'react';
import { getImportHistory } from '../api/movieApi';

const ImportHistory = () => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchHistory = async () => {
        try {
            setLoading(true);
            const response = await getImportHistory();
            // Сортируем историю по дате, чтобы новые были сверху
            setHistory(response.data.sort((a, b) => new Date(b.importDate) - new Date(a.importDate)));
            setError(null);
        } catch (err) {
            setError('Failed to fetch import history.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchHistory();
    }, []);

    const handleDownload = (objectName) => {
        window.location.href = `/api/import/file/${objectName}`;
    };

    if (loading) return <p>Loading history...</p>;
    if (error) return <p style={{ color: 'red' }}>{error}</p>;

    return (
        <div className="import-history-container">
            <h2>Import History</h2>
            <button onClick={fetchHistory} className="button" style={{marginBottom: '15px'}}>Refresh History</button>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Imported Count</th>
                    <th>Details</th>
                    <th>File</th>
                </tr>
                </thead>
                <tbody>
                {history.length > 0 ? history.map(item => (
                    <tr key={item.id}>
                        <td>{item.id}</td>
                        <td>{new Date(item.importDate).toLocaleString()}</td>
                        <td style={{ color: item.status === 'SUCCESS' ? 'green' : 'red' }}>{item.status}</td>
                        <td>{item.status === 'SUCCESS' ? item.importedCount : 'N/A'}</td>
                        <td style={{maxWidth: '300px', overflowWrap: 'break-word'}}>{item.details}</td>

                        <td>
                            {item.objectName ? (
                                <button
                                    className="button button-secondary"
                                    style={{fontSize: '0.8rem', padding: '5px 10px'}}
                                    onClick={() => handleDownload(item.objectName)}
                                >
                                    Download
                                </button>
                            ) : (
                                <span style={{color: '#ccc'}}>-</span>
                            )}
                        </td>

                    </tr>
                )) : (
                    <tr>
                        <td colSpan="6">No import history found.</td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
};

export default ImportHistory;