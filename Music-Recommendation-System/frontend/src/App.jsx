import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import MainMenu from "./pages/MainMenu";
import SpotifyCallbackPage from "./pages/SpotifyCallbackPage";

function PrivateRoute({ children }) {
    const spotifyId = sessionStorage.getItem("spotify_id");
    if (!spotifyId) {
        return <Navigate to="/" replace />;
    }
    return children;
}

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/callback" element={<SpotifyCallbackPage />} />
                <Route
                    path="/beatbridge"
                    element={
                        <PrivateRoute>
                            <MainMenu />
                        </PrivateRoute>
                    }
                />
                <Route path="*" element={<Navigate to="/" />} />
            </Routes>
        </Router>
    );
}

export default App;