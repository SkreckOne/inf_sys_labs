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
                </tr>
                </thead>
                <tbody>
                {history.length > 0 ? history.map(item => (
                    <tr key={item.id}>
                        <td>{item.id}</td>
                        <td>{new Date(item.importDate).toLocaleString()}</td>
                        <td style={{ color: item.status === 'SUCCESS' ? 'green' : 'red' }}>{item.status}</td>
                        <td>{item.status === 'SUCCESS' ? item.importedCount : 'N/A'}</td>
                        <td style={{maxWidth: '400px', overflowWrap: 'break-word'}}>{item.details}</td>
                    </tr>
                )) : (
                    <tr>
                        <td colSpan="5">No import history found.</td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
};

export default ImportHistory;