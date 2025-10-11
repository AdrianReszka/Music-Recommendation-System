import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../components/TopBar.jsx';
import LovedTracksPanel from "./LovedTracksPanel.jsx";
import RecommendationsPanel from "./RecommendationsPanel.jsx";
import PlaylistsPanel from "./PlaylistsPanel.jsx";
import LinkedAccountsPanel from "./LinkedAccountsPanel.jsx";

function MainMenu() {
    const [activeView, setActiveView] = useState('loved');
    const [username, setUsername] = useState('');
    const [menuOpen, setMenuOpen] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const spotifyId = sessionStorage.getItem("spotify_id");
        if (!spotifyId) {
            navigate("/", { replace: true });
        }

        const storedUsername = sessionStorage.getItem("spotify_username");
        setUsername(storedUsername || "Unknown");
    }, [navigate]);

    return (
        <div
            className="w-screen h-screen flex flex-col overflow-hidden"
            style={{
                backgroundImage: 'url(/images/background.jpg)',
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                backgroundAttachment: 'fixed'
            }}
        >
            <TopBar username={username} setActiveView={setActiveView} onMenuToggle={setMenuOpen} />

            <div className="relative flex-1 text-white p-8 overflow-y-auto bg-black/40 backdrop-blur-md m-0 rounded-lg transition-colors duration-500">
                <div
                    className={`absolute inset-0 bg-black transition-opacity duration-500 pointer-events-none ${
                        menuOpen ? "opacity-40" : "opacity-0"
                    }`}
                ></div>

                {activeView === 'loved' && <LovedTracksPanel />}
                {activeView === 'recommend' && <RecommendationsPanel />}
                {activeView === 'playlist' && <PlaylistsPanel />}
                {activeView === 'linked accounts' && <LinkedAccountsPanel />}
            </div>
        </div>
    );
}

export default MainMenu;
