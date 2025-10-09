import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../components/TopBar.jsx';
import LovedTracksPanel from "./LovedTracksPanel.jsx";
import RecommendationsPanel from "./RecommendationsPanel.jsx";
import PlaylistsPanel from "./PlaylistsPanel.jsx";

function MainMenu() {
    const [activeView, setActiveView] = useState('loved');
    const [username, setUsername] = useState('');
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
            <TopBar username={username} setActiveView={setActiveView} />

            <div className="flex-1 text-white p-8 overflow-y-auto bg-black/40 backdrop-blur-md m-0 rounded-lg">
                {activeView === 'loved' && <LovedTracksPanel />}
                {activeView === 'recommend' && <RecommendationsPanel />}
                {activeView === 'playlist' && <PlaylistsPanel />}
            </div>
        </div>
    );
}

export default MainMenu;
