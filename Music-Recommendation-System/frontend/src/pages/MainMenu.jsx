import * as React from 'react';
import { useEffect, useState } from 'react';
import MenuButton from '../components/MenuButton.jsx';
import TopBar from "../components/TopBar.jsx";

function App() {
    const [message, setMessage] = useState('');

    useEffect(() => {
        document.documentElement.classList.add("overflow-x-hidden", "overflow-y-hidden", "m-0", "p-0");
        document.body.classList.add("overflow-x-hidden", "overflow-y-hidden", "m-0", "p-0");
    }, []);

    const handleDownloadLoved = async () => {
        try {
            const res = await fetch('musicapp/lastfm/loved');
            if (!res.ok) {
                setMessage(`Błąd: ${res.status}`);
                return;
            }
            const text = await res.text();
            setMessage(text);
        } catch (error) {
            setMessage(`Błąd połączenia: ${error.message}`);
        }
    };

    const handleGenerateRecommendations = () => {
        setMessage("Wygenerowano rekomendacje (placeholder)");
    };

    const handleSaveAsPlaylist = () => {
        setMessage("Zapisano playlistę (placeholder)");
    };

    const handleLogout = () => {
        setMessage("Wylogowano");
    };

    return (
        <>
            <TopBar username="Snusik" />

            <div className="h-[calc(100vh-60px)] w-screen mt-[60px] flex flex-col justify-center items-center gap-4 text-center">
                <MenuButton label="Download loved tracks" onClick={handleDownloadLoved} />
                <MenuButton label="Generate recommendations" onClick={handleGenerateRecommendations} />
                <MenuButton label="Save as playlist" onClick={handleSaveAsPlaylist} />
                <MenuButton label="Logout" onClick={handleLogout} />
                {message && <p className="mt-4 text-sm text-white">{message}</p>}
            </div>
        </>
    );
}

export default App;
