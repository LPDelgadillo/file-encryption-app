const Header = () => {
    return (
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🔐</div>
            <h1 style={{
                color: '#ccd6f6',
                fontSize: '2rem',
                fontWeight: '700',
                margin: '0 0 0.5rem',
                fontFamily: 'monospace'
            }}>
                File Encryption App
            </h1>
            <p style={{
                color: '#8892b0',
                fontSize: '0.95rem',
                margin: '0 0 1.5rem',
                maxWidth: '400px'
            }}>
                Encrypt and decrypt your files using{' '}
                <span style={{ color: '#64ffda' }}>AES-256-GCM</span>
                {' '}— the same standard used by banks and governments.
            </p>
            <div style={{
                display: 'flex',
                gap: '0.5rem',
                justifyContent: 'center',
                flexWrap: 'wrap'
            }}>
                {['AES-256-GCM', 'React', 'Java Spring Boot', 'RTK Query'].map((tech) => (
                    <span key={tech} style={{
                        background: '#112240',
                        color: '#64ffda',
                        border: '1px solid #64ffda44',
                        borderRadius: '20px',
                        padding: '4px 12px',
                        fontSize: '0.75rem',
                        fontFamily: 'monospace'
                    }}>
                        {tech}
                    </span>
                ))}
            </div>
        </div>
    )
}

export default Header