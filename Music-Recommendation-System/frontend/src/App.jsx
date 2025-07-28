import React, { useState } from 'react';
import MainMenu from './pages/MainMenu';
import LandingPage from './pages/LandingPage';

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    const handleSpotifyLogin = () => {
        setIsLoggedIn(true);
    };

    const handleLogout = () => {
        setIsLoggedIn(false);
    };

    return isLoggedIn
        ? <MainMenu onLogout={handleLogout} />
        : <LandingPage onSpotifyLogin={handleSpotifyLogin} />;
}

export default App;
