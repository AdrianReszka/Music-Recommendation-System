import { useEffect, useState } from 'react';
import TopBar from '../components/TopBar.jsx';
import Sidebar from '../components/Sidebar.jsx';
import LovedTracksPanel from "./LovedTracksPanel.jsx";
import RecommendationsPanel from "./RecommendationsPanel.jsx";
import PlaylistsPanel from "./PlaylistsPanel.jsx";

function MainMenu() {
    const [activeView, setActiveView] = useState('loved');
    const [username, setUsername] = useState('');

    useEffect(() => {
        const storedUsername = localStorage.getItem("spotify_username");
        setUsername(storedUsername || 'Unknown');
    }, []);

    return (
        <>
            <TopBar username={username} />

            <div className="w-screen mt-[10vh] h-[90vh] flex flex-row overflow-hidden shadow-md">
                <Sidebar setActiveView={setActiveView} />

                <div className="flex-1 bg-[#1f1f1f] text-white p-8 overflow-y-auto h-full shadow-md">
                    {activeView === 'loved' && <LovedTracksPanel />}
                    {activeView === 'recommend' && <RecommendationsPanel />}
                    {activeView === 'playlist' && <PlaylistsPanel />}
                </div>
            </div>
        </>
    );
}

export default MainMenu;
