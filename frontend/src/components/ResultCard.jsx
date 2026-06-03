/**
 * ResultCard — muestra el resultado de una operación.
 *
 * Recibe el blob del archivo procesado y lo descarga
 * automáticamente usando una URL temporal del navegador.
 */
const ResultCard = ({ result, operation, onReset }) => {
    const isEncrypt = operation === 'encrypt'

    return (
        <div style={{
            background: 'linear-gradient(135deg, #112240, #0a192f)',
            border: `1px solid ${isEncrypt ? '#64ffda' : '#a78bfa'}`,
            borderRadius: '12px',
            padding: '2rem',
            textAlign: 'center',
            marginTop: '1.5rem'
        }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>
                {isEncrypt ? '🔐' : '🔓'}
            </div>
            <h3 style={{
                color: isEncrypt ? '#64ffda' : '#a78bfa',
                fontSize: '1.2rem',
                margin: '0 0 0.5rem'
            }}>
                {isEncrypt ? 'File encrypted!' : 'File decrypted!'}
            </h3>
            <p style={{
                color: '#8892b0',
                fontSize: '0.875rem',
                margin: '0 0 0.25rem'
            }}>
                ⬇️ <strong style={{ color: '#ccd6f6' }}>{result.fileName}</strong> downloaded automatically
            </p>
            <p style={{
                color: '#8892b044',
                fontSize: '0.75rem',
                margin: '0 0 1.5rem'
            }}>
                Check your downloads folder
            </p>
            <button
                onClick={onReset}
                style={{
                    background: 'transparent',
                    color: '#8892b0',
                    border: '1px solid #8892b0',
                    borderRadius: '8px',
                    padding: '0.75rem 1.5rem',
                    fontSize: '0.875rem',
                    cursor: 'pointer',
                }}
            >
                ↩ Process another file
            </button>
        </div>
    )
}

export default ResultCard