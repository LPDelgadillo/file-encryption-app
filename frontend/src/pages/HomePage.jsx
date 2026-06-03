import EncryptionForm from '../components/EncryptionForm'
import Header from '../components/Header'
import Footer from '../components/Footer'

const HomePage = () => {
    return (
        <div style={{
            minHeight: '100vh',
            background: '#0a192f',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            padding: '3rem 1rem'
        }}>
            <Header />

            <div style={{
                width: '100%',
                maxWidth: '520px',
                background: '#112240',
                borderRadius: '16px',
                padding: '2rem',
                border: '1px solid #64ffda22'
            }}>
                <EncryptionForm />
            </div>

            <Footer />
        </div>
    )
}

export default HomePage