import * as React from 'react';
import { useNavigate } from "react-router-dom";

function TopBar({ username, setActiveView, onMenuToggle }) {
    const [isMenuOpen, setIsMenuOpen] = React.useState(false);
    const navigate = useNavigate();

    React.useEffect(() => {
        const handleResize = () => {
            if (window.innerWidth >= 640 && isMenuOpen) {
                setIsMenuOpen(false);
                if (onMenuToggle) onMenuToggle(false);
            }
        };

        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, [isMenuOpen, onMenuToggle]);


    const handleLogout = async () => {
        sessionStorage.removeItem("spotify_id");
        sessionStorage.removeItem("spotify_username");
        navigate("/", { replace: true });
    };

    const handleViewChange = (view) => {
        setActiveView(view);
        setIsMenuOpen(false);
        if (onMenuToggle) onMenuToggle(false);
    };

    const toggleMenu = () => {
        const newState = !isMenuOpen;
        setIsMenuOpen(newState);
        if (onMenuToggle) onMenuToggle(newState);
    };

    return (
        <div
            className={`fixed top-0 left-0 w-full h-[8vh] bg-[#1DB954] flex justify-between items-center px-4 sm:px-6 lg:px-8 
                        text-gray-300 text-[1.25rem] font-bold z-20`}
        >
            <div className="text-[#191414] text-base sm:text-lg md:text-xl lg:text-2xl whitespace-nowrap">
                Beat Bridge
            </div>

            <div className="block sm:hidden">
                <button
                    onClick={toggleMenu}
                    className="text-[#191414] focus:outline-none cursor-pointer hover:text-white transition duration-300"
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        className={`w-8 h-8 transform transition-transform duration-300 ${
                            isMenuOpen ? "rotate-90" : "rotate-0"
                        }`}
                    >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                </button>
            </div>


            <div className="hidden sm:flex space-x-4 md:space-x-6 lg:space-x-8 flex-wrap justify-center items-center">
                <button
                    onClick={() => handleViewChange('loved')}
                    className="text-[#191414] cursor-pointer hover:text-white text-base md:text-lg lg:text-xl transition duration-300 focus:outline-none focus:ring-0 whitespace-nowrap">
                    Loved Tracks
                </button>
                <button
                    onClick={() => handleViewChange('recommend')}
                    className="text-[#191414] cursor-pointer hover:text-white text-base md:text-lg lg:text-xl transition duration-300 focus:outline-none focus:ring-0 whitespace-nowrap">
                    Recommendations
                </button>
                <button
                    onClick={() => handleViewChange('playlist')}
                    className="text-[#191414] cursor-pointer hover:text-white text-base md:text-lg lg:text-xl transition duration-300 focus:outline-none focus:ring-0 whitespace-nowrap">
                    Playlists
                </button>
                <button
                    onClick={() => handleViewChange('linked accounts')}
                    className="text-[#191414] cursor-pointer hover:text-white text-base md:text-lg lg:text-xl transition duration-300 focus:outline-none focus:ring-0 whitespace-nowrap">
                    Linked Accounts
                </button>
                <button
                    onClick={handleLogout}
                    className="text-[#191414] cursor-pointer hover:text-red-500 text-base md:text-lg lg:text-xl transition duration-300 focus:outline-none focus:ring-0 whitespace-nowrap">
                    Logout
                </button>
            </div>

            <div
                className={`${
                    isMenuOpen
                        ? "max-h-[300px] opacity-100 translate-y-0"
                        : "max-h-0 opacity-0 -translate-y-4"
                } sm:hidden absolute top-[8vh] left-0 w-full bg-[#17a34a] p-4 space-y-4 text-center overflow-hidden
                   transition-all duration-500 ease-out shadow-lg transform z-30`}
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
                    onClick={() => handleViewChange('linked accounts')}
                    className="text-[#191414] cursor-pointer hover:text-white text-xl transition duration-300 focus:outline-none focus:ring-0 w-full">
                    Linked Accounts
                </button>
                <button
                    onClick={handleLogout}
                    className="text-[#191414] cursor-pointer hover:text-red-500 text-xl sm:text-xl transition duration-300 focus:outline-none focus:ring-0 w-full">
                    Logout
                </button>
            </div>

            <div className="text-[#191414] text-base sm:text-lg md:text-xl lg:text-2xl">Hello, {username}!</div>
        </div>
    );
}

export default TopBar;
