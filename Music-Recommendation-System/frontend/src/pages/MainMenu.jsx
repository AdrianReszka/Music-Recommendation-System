import * as React from 'react';
import { useState } from 'react';
import TopBar from '../components/TopBar.jsx';
import Sidebar from '../components/Sidebar.jsx';
import LovedTracksPanel from "./LovedTracksPanel.jsx";

function MainMenu() {
    const [activeView, setActiveView] = useState('loved');

    return (
        <>
            <TopBar username="Snusik" />

            <div className="w-screen mt-[10vh] h-[90vh] flex flex-row overflow-hidden shadow-md">

                <Sidebar setActiveView={setActiveView} />

                <div className="flex-1 bg-[#1f1f1f] text-white p-8 overflow-y-auto h-full shadow-md">
                    {activeView === 'loved' && <LovedTracksPanel />}
                    {activeView === 'recommend' && <p className="text-xl">🧠 Tutaj będą rekomendacje</p>}
                    {activeView === 'playlist' && <p className="text-xl">🎧 Tutaj stworzysz playlistę</p>}
                    {activeView === 'logout' && <p className="text-xl text-red-400">Wylogowano</p>}
                </div>

            </div>
        </>
    );
}

export default MainMenu;
