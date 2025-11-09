import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

export default function SpotifyCallbackPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        const spotifyId = searchParams.get("spotifyId");
        const username = searchParams.get("username");

        console.log("spotifyId:", spotifyId);
        console.log("username:", username);

        if (spotifyId && username) {
            sessionStorage.setItem("spotify_id", spotifyId);
            sessionStorage.setItem("spotify_username", username);
            navigate("/beatbridge", { replace: true });
        } else {
            alert("Spotify login failed");
            navigate("/");
        }
    }, [navigate, searchParams]);
}