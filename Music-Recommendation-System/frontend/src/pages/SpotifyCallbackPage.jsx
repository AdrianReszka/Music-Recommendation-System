import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function SpotifyCallbackPage() {
    const navigate = useNavigate();

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const username = urlParams.get("username");

        if (username) {
            localStorage.setItem("spotify_username", username);
            navigate("/beatbridge");
        } else {
            alert("Login failed.");
            navigate("/");
        }
    }, [navigate]);

    return <p className="text-white text-center mt-10">Logging in to Spotify...</p>;
}
