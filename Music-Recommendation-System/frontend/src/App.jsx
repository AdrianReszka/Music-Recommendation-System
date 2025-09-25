import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import MainMenu from "./pages/MainMenu";
import SpotifyCallbackPage from "./pages/SpotifyCallbackPage";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/callback" element={<SpotifyCallbackPage />} />
                <Route path="/beatbridge" element={<MainMenu />}/>
                <Route path="*" element={<Navigate to="/" />} />
            </Routes>
        </Router>
    );
}

export default App;