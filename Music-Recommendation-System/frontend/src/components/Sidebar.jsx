import * as React from 'react';
import SidebarButton from './SidebarButton';

function Sidebar({ setActiveView }) {

    const handleLogout = async () => {
        const spotifyId = localStorage.getItem("spotify_id");
        if (spotifyId) {
            try {
                await fetch(`/musicapp/spotify/logout?spotifyId=${spotifyId}`, {
                    method: "POST",
                });
            } catch (err) {
                console.error("Logout failed", err);
            }
        }

        localStorage.removeItem("spotify_id");
        localStorage.removeItem("username");

        window.location.href = "/";
    };

    return (
        <div className="h-full w-full max-w-[400px] min-w-[240px] bg-[#2a2a2a] shadow-md flex flex-col">

            <div className="flex-1 flex flex-col items-center justify-center">
                <SidebarButton text="Loved Tracks" onClick={() => setActiveView('loved')} />
            </div>
            <hr className="w-[80%] mx-auto border-t border-gray-500" />

            <div className="flex-1 flex flex-col items-center justify-center">
                <SidebarButton text="Recommendations" onClick={() => setActiveView('recommend')} />
            </div>
            <hr className="w-[80%] mx-auto border-t border-gray-500" />

            <div className="flex-1 flex flex-col items-center justify-center">
                <SidebarButton text="Create Playlist" onClick={() => setActiveView('playlist')} />
            </div>
            <hr className="w-[80%] mx-auto border-t border-gray-500" />

            <div className="flex-1 flex flex-col items-center justify-center">
                <SidebarButton text="Logout" onClick={handleLogout} isLogout />
            </div>

        </div>
    );
}

export default Sidebar;
