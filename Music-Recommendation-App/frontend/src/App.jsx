import { useState } from 'react'

function App() {
    const [message, setMessage] = useState('')

    const handleClick = async () => {
        try {
            const res = await fetch('musicapp/tracks/hello')
            if (!res.ok) {
                setMessage(`Błąd: ${res.status}`)
                return
            }
            const text = await res.text()
            setMessage(text)
        } catch (error) {
            setMessage(`Błąd połączenia: ${error.message}`)
        }
    }

    return (
        <div style={{
            height: '100vh',
            width: '100vw',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            flexDirection: 'column',
            textAlign: 'center',
            gap: '1rem',
        }}>
            <button
                onClick={handleClick}
                style={{
                    minWidth: '250px',
                    padding: '0.75rem 1.5rem',
                    fontWeight: 'bold',
                    borderRadius: '8px',
                    cursor: 'pointer',
                }}
            >
                Pobierz wiadomość z backendu
            </button>
            {message && <p>{message}</p>}
        </div>
    )
}

export default App
