import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

export default function SpotifyCallbackPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        const spotifyId = searchParams.get("spotifyId");
        const username = searchParams.get("username");

        if (spotifyId && username) {
            localStorage.setItem("spotify_id", spotifyId);
            localStorage.setItem("spotify_username", username);
            navigate("/beatbridge");
        } else {
            alert("Spotify login failed");
            navigate("/");
        }
    }, [navigate, searchParams]);

    return (
        <div className="text-white text-2xl text-center mt-20">
            Logging in with Spotify...
        </div>
    );
}