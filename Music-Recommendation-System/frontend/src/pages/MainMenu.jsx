import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../components/TopBar.jsx';
import LovedTracksPanel from "./LovedTracksPanel.jsx";
import RecommendationsPanel from "./RecommendationsPanel.jsx";
import PlaylistsPanel from "./PlaylistsPanel.jsx";
import LinkedAccountsPanel from "./LinkedAccountsPanel.jsx";
import DimOverlay from '../components/DimOverlay.jsx';
import { UIProvider } from '../context/UIContext.jsx';

export default function MainMenu() {
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
        <UIProvider menuOpen={menuOpen} setMenuOpen={setMenuOpen}>
            <div
                className="w-screen h-screen flex flex-col overflow-hidden"
                style={{
                    backgroundImage: 'url(/images/background.jpg)',
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    backgroundAttachment: 'fixed'
                }}
            >
                <div className="relative z-[1100]">
                    <TopBar username={username} setActiveView={(view) => {
                        setActiveView(view);
                        setMenuOpen(false);
                    }} onMenuToggle={setMenuOpen} />
                </div>

                <DimOverlay visible={menuOpen} onClick={() => setMenuOpen(false)} zIndex={1000} />

                <div className="relative flex-1 text-white p-8 overflow-y-auto backdrop-blur-md m-0 rounded-lg transition-colors duration-500">
                    {activeView === 'loved' && <LovedTracksPanel />}
                    {activeView === 'recommend' && <RecommendationsPanel />}
                    {activeView === 'playlist' && <PlaylistsPanel />}
                    {activeView === 'linked accounts' && <LinkedAccountsPanel />}
                </div>
            </div>
        </UIProvider>
    );
}