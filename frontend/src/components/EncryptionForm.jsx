import { useState, useRef } from 'react'
import { useEncryptDataMutation, useDecryptDataMutation } from '../redux/peticiones/encryptionApi'
import ResultCard from './ResultCard'

/**
 * EncryptionForm — formulario principal de la app.
 *
 * Maneja:
 * - Selección de archivo (drag & drop o click)
 * - Input de clave secreta
 * - Toggle encrypt/decrypt
 * - Estados de carga y error
 * - Resultado con descarga
 */
const EncryptionForm = () => {
    const [file, setFile] = useState(null)
    const [secretKey, setSecretKey] = useState('')
    const [operation, setOperation] = useState('encrypt')
    const [dragOver, setDragOver] = useState(false)
    const [result, setResult] = useState(null)
    const fileInputRef = useRef(null)

    const [validationError, setValidationError] = useState(null)

    // Hooks de RTK Query — manejan loading/error automáticamente
    const [encryptData, { isLoading: encrypting, error: encryptError }] = useEncryptDataMutation()
    const [decryptData, { isLoading: decrypting, error: decryptError }] = useDecryptDataMutation()

    const isLoading = encrypting || decrypting
    const error = encryptError || decryptError


    const MAX_SIZE_MB = 5
    const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024

    const ALLOWED_TYPES = {
        // Documentos
        'application/pdf': '.pdf',
        'application/msword': '.doc',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx',
        'application/vnd.ms-excel': '.xls',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '.xlsx',
        'application/vnd.ms-powerpoint': '.ppt',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation': '.pptx',
        'text/plain': '.txt',
        // Imágenes
        'image/jpeg': '.jpg',
        'image/png': '.png',
        'image/gif': '.gif',
        'image/webp': '.webp',
        // Audio/Video
        'audio/mpeg': '.mp3',
        'video/mp4': '.mp4',
        'video/x-msvideo': '.avi',
        // Código
        'application/javascript': '.js',
        'text/javascript': '.js',
        'application/json': '.json',
        'application/xml': '.xml',
        'text/xml': '.xml',
        'text/x-python': '.py',
        'text/x-java-source': '.java',
        // Comprimidos
        'application/zip': '.zip',
        'application/x-rar-compressed': '.rar',
        'application/vnd.rar': '.rar',
        // Archivos ya encriptados (para decrypt)
        'application/octet-stream': '.encrypted',
    }

    // ── Handlers de archivo ──────────────────────────────
    const validateFile = (selectedFile, currentOperation) => {
        // Para decrypt solo acepta .encrypted
        if (currentOperation === 'decrypt') {
            if (!selectedFile.name.endsWith('.encrypted')) {
                return 'For decryption, please upload a .encrypted file'
            }
            if (selectedFile.size > MAX_SIZE_BYTES) {
                return `File size exceeds ${MAX_SIZE_MB}MB limit`
            }
            return null
        }

        // Para encrypt valida tipo y tamaño
        if (selectedFile.size > MAX_SIZE_BYTES) {
            return `File size exceeds ${MAX_SIZE_MB}MB limit`
        }
        if (!ALLOWED_TYPES[selectedFile.type]) {
            return `File type not allowed. Supported: PDF, DOCX, XLSX, JPG, PNG, MP3, MP4, ZIP, RAR, JSON, and more`
        }
        return null
    }

    const handleFileChange = (e) => {
        const selected = e.target.files[0]
        if (!selected) return
        const error = validateFile(selected, operation)
        if (error) {
            setValidationError(error)
            setFile(null)
            return
        }
        setValidationError(null)
        setFile(selected)
    }

    const handleDrop = (e) => {
        e.preventDefault()
        setDragOver(false)
        const dropped = e.dataTransfer.files[0]
        if (!dropped) return
        const error = validateFile(dropped, operation)
        if (error) {
            setValidationError(error)
            setFile(null)
            return
        }
        setValidationError(null)
        setFile(dropped)
    }

    const handleDragOver = (e) => {
        e.preventDefault()
        setDragOver(true)
    }

    // ── Submit ────────────────────────────────────────────
    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!file || !secretKey) return
        setResult(null)

        try {
            let response
            if (operation === 'encrypt') {
                response = await encryptData({ file, secretKey }).unwrap()
            } else {
                response = await decryptData({ file, secretKey }).unwrap()
            }

            // Descarga automática inmediata
            const url = window.URL.createObjectURL(response.blob)
            const link = document.createElement('a')
            link.href = url
            link.download = response.fileName
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
            window.URL.revokeObjectURL(url)

            // Limpia el formulario
            setFile(null)
            setSecretKey('')
            if (fileInputRef.current) fileInputRef.current.value = ''

            // Muestra el resultado
            setResult(response)

        } catch (err) {
            console.error('Operation failed:', err)
        }
    }

    const handleReset = () => {
        setFile(null)
        setSecretKey('')
        setResult(null)
        setValidationError(null)
        if (fileInputRef.current) fileInputRef.current.value = ''
    }

    // ── Estilos ───────────────────────────────────────────
    const accentColor = operation === 'encrypt' ? '#64ffda' : '#a78bfa'

    return (
        <div style={{ width: '100%', maxWidth: '520px', margin: '0 auto' }}>

            {/* Toggle encrypt / decrypt */}
            <div style={{
                display: 'flex',
                background: '#112240',
                borderRadius: '10px',
                padding: '4px',
                marginBottom: '1.5rem'
            }}>
                {['encrypt', 'decrypt'].map((op) => (
                    <button
                        key={op}
                        onClick={() => { setOperation(op); setResult(null) }}
                        style={{
                            flex: 1,
                            padding: '0.6rem',
                            border: 'none',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '0.875rem',
                            fontWeight: '600',
                            transition: 'all 0.2s',
                            background: operation === op ? accentColor : 'transparent',
                            color: operation === op ? '#0a192f' : '#8892b0',
                        }}
                    >
                        {op === 'encrypt' ? '🔐 Encrypt' : '🔓 Decrypt'}
                    </button>
                ))}
            </div>

            <form onSubmit={handleSubmit}>

                {/* Drag & Drop zone */}
                <div
                    onClick={() => fileInputRef.current?.click()}
                    onDrop={handleDrop}
                    onDragOver={handleDragOver}
                    onDragLeave={() => setDragOver(false)}
                    style={{
                        border: `2px dashed ${dragOver ? accentColor : file ? accentColor + '88' : '#8892b0'}`,
                        borderRadius: '12px',
                        padding: '2rem',
                        textAlign: 'center',
                        cursor: 'pointer',
                        marginBottom: '1rem',
                        background: dragOver ? `${accentColor}11` : 'transparent',
                        transition: 'all 0.2s'
                    }}
                >
                    <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
                        {file ? '📄' : '📁'}
                    </div>
                    <p style={{ color: file ? accentColor : '#8892b0', margin: 0, fontSize: '0.875rem' }}>
                        {file ? file.name : 'Click or drag & drop your file here'}
                    </p>
                    {file && (
                        <p style={{ color: '#8892b0', margin: '4px 0 0', fontSize: '0.75rem' }}>
                            {(file.size / 1024).toFixed(1)} KB
                        </p>
                    )}
                    <input
                        ref={fileInputRef}
                        type="file"
                        onChange={handleFileChange}
                        style={{ display: 'none' }}
                    />
                </div>

                {/* Secret key input */}
                <div style={{ marginBottom: '1rem' }}>
                    <label style={{
                        display: 'block',
                        color: '#8892b0',
                        fontSize: '0.75rem',
                        marginBottom: '6px',
                        textTransform: 'uppercase',
                        letterSpacing: '1px'
                    }}>
                        Secret Key (min. 8 characters)
                    </label>
                    <input
                        type="password"
                        value={secretKey}
                        onChange={(e) => setSecretKey(e.target.value)}
                        placeholder="Enter your secret key..."
                        style={{
                            width: '100%',
                            padding: '0.75rem 1rem',
                            background: '#112240',
                            border: `1px solid ${secretKey.length >= 8 ? accentColor + '88' : '#8892b0'}`,
                            borderRadius: '8px',
                            color: '#ccd6f6',
                            fontSize: '0.875rem',
                            outline: 'none',
                            boxSizing: 'border-box'
                        }}
                    />
                </div>

                {/* Error de validación local */}
                {validationError && (
                    <div style={{
                        background: '#ff444422',
                        border: '1px solid #ff4444',
                        borderRadius: '8px',
                        padding: '0.75rem 1rem',
                        color: '#ff4444',
                        fontSize: '0.875rem',
                        marginBottom: '1rem'
                    }}>
                        ⚠️ {validationError}
                    </div>
                )}

                {/* Error message */}
                {error && (
                    <div style={{
                        background: '#ff444422',
                        border: '1px solid #ff4444',
                        borderRadius: '8px',
                        padding: '0.75rem 1rem',
                        color: '#ff4444',
                        fontSize: '0.875rem',
                        marginBottom: '1rem'
                    }}>
                        ⚠️ {error?.error || 'Something went wrong. Check your key and try again.'}
                    </div>
                )}

                {/* Submit button */}
                <button
                    type="submit"
                    disabled={!file || secretKey.length < 8 || isLoading}
                    style={{
                        width: '100%',
                        padding: '0.875rem',
                        background: !file || secretKey.length < 8 || isLoading
                            ? '#8892b044'
                            : accentColor,
                        color: '#0a192f',
                        border: 'none',
                        borderRadius: '8px',
                        fontSize: '0.875rem',
                        fontWeight: '700',
                        cursor: !file || secretKey.length < 8 || isLoading ? 'not-allowed' : 'pointer',
                        transition: 'all 0.2s'
                    }}
                >
                    {isLoading
                        ? '⏳ Processing...'
                        : operation === 'encrypt'
                            ? '🔐 Encrypt File'
                            : '🔓 Decrypt File'
                    }
                </button>

            </form>

            {/* Resultado */}
            {result && (
                <ResultCard
                    result={result}
                    operation={operation}
                    onReset={handleReset}
                />
            )}
        </div>
    )
}

export default EncryptionForm