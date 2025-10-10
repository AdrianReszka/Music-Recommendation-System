import * as React from 'react';
import { useNavigate } from "react-router-dom";

function TopBar({ username, setActiveView }) {
    const [isMenuOpen, setIsMenuOpen] = React.useState(false);
    const navigate = useNavigate();

    const handleLogout = async () => {
        sessionStorage.removeItem("spotify_id");
        sessionStorage.removeItem("spotify_username");

        navigate("/", { replace: true });
    };

    const handleViewChange = (view) => {
        setActiveView(view);
        setIsMenuOpen(false);
    };

    return (
        <div
            className={`fixed top-0 left-0 w-full h-[8vh] bg-[#1DB954] flex justify-between items-center px-8 text-gray-300 text-[1.25rem] font-bold z-10 shadow-md ${isMenuOpen ? 'border-b-2 border-black' : ''}`}
        >
            <div className="text-[#191414] text-xl sm:text-2xl">BeatBridge</div>

            <div className="block sm:hidden">
                <button
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                    className="text-[#191414] focus:outline-none cursor-pointer hover:text-white transition duration-300"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-8 h-8">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                </button>
            </div>

            <div className="hidden sm:flex space-x-10 sm:space-x-8 flex-wrap justify-center">
                <button
                    onClick={() => handleViewChange('loved')}
                    className="text-[#191414] cursor-pointer hover:text-white text-xl sm:text-xl transition duration-300 focus:outline-none focus:ring-0">
                    Loved Tracks
                </button>
                <button
                    onClick={() => handleViewChange('recommend')}
                    className="text-[#191414] cursor-pointer hover:text-white text-lg sm:text-xl transition duration-300 focus:outline-none focus:ring-0">
                    Recommendations
                </button>
                <button
                    onClick={() => handleViewChange('playlist')}
                    className="text-[#191414] cursor-pointer hover:text-white text-lg sm:text-xl transition duration-300 focus:outline-none focus:ring-0">
                    Playlists
                </button>
                <button
                    onClick={handleLogout}
                    className="text-[#191414] cursor-pointer hover:text-red-500 text-lg sm:text-xl transition duration-300 focus:outline-none focus:ring-0">
                    Logout
                </button>
            </div>

            <div
                className={`${
                    isMenuOpen ? "max-h-[300px] opacity-100" : "max-h-0 opacity-0"
                } sm:hidden absolute top-[8vh] left-0 w-full bg-[#1DB954] p-4 space-y-4 text-center overflow-hidden transition-all duration-500 shadow-lg`}
            >
                <button
                    onClick={() => handleViewChange('loved')}
                    className="text-[#191414] cursor-pointer hover:text-white text-xl sm:text-xl transition duration-300 focus:outline-none focus:ring-0 w-full">
                    Loved Tracks
                </button>
                <button
                    onClick={() => handleViewChange('recommend')}
                    className="text-[#191414] cursor-pointer hover:text-white text-xl sm:text-xl transition duration-300 focus:outline-none focus:ring-0 w-full">
                    Recommendations
                </button>
                <button
                    onClick={() => handleViewChange('playlist')}
                    className="text-[#191414] cursor-pointer hover:text-white text-xl sm:text-xl transition duration-300 focus:outline-none focus:ring-0 w-full">
                    Playlists
                </button>
                <button
                    onClick={handleLogout}
                    className="text-[#191414] cursor-pointer hover:text-red-500 text-xl sm:text-xl transition duration-300 focus:outline-none focus:ring-0 w-full">
                    Logout
                </button>
            </div>

            <div className="text-[#191414] text-xl sm:text-2xl">Hello, {username}!</div>
        </div>
    );
}

export default TopBar;
