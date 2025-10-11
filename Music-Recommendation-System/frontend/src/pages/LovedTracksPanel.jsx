import React, {useEffect, useState} from 'react';
import UsernameInput from '../components/UsernameInput';
import PanelButton from "../components/PanelButton.jsx";

export default function LovedTracksPanel() {
    const [username, setUsername] = useState('');
    const [savedAs, setSavedAs] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [stats, setStats] = useState({ totalTracks: 0, averageTracksPerUser: 0, totalLinkedAccounts: 0 });

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const res = await fetch("/musicapp/stats");
                if (res.ok) {
                    const data = await res.json();
                    setStats(data);
                }
            } catch (err) {
                console.error("Failed to fetch stats:", err);
            }
        };
        fetchStats();
    }, []);

    const handleDownload = async () => {
        if (!username.trim()) {
            alert("Please enter your Last.fm username.");
            return;
        }

        const spotifyId = sessionStorage.getItem("spotify_id");
        if (!spotifyId) {
            alert("You must be logged in with Spotify before importing loved tracks.");
            return;
        }

        setIsLoading(true);
        setSavedAs("");

        try {
            const response = await fetch(`/musicapp/user-tracks/import?username=${username}&spotifyId=${spotifyId}`, {
                method: 'GET',
            });

            if (response.status === 200) {
                setSavedAs(`${username} loved tracks`);
            } else if (response.status === 204) {
                alert(`User "${username}" exists but has no loved tracks.`);
                setUsername("");
            } else if (response.status === 404) {
                alert(`User "${username}" was not found on Last.fm.`);
                setUsername("");
            } else {
                const text = await response.text();
                alert(`Unexpected error: ${text}`);
                setUsername("");
            }
        } catch (err) {
            console.error("Error while fetching loved tracks:", err);
            alert("Error while fetching loved tracks");
            setUsername("");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full min-h-[100vh] text-white flex items-center justify-center px-4">
            <div className="grid md:grid-cols-2 items-center justify-center w-full max-w-[calc(100%-2*12.5vw)] gap-12">

                <section className="w-full max-w-[44rem] mx-auto text-center md:text-left">
                    <h2 className="text-3xl sm:text-4xl md:text-5xl font-extrabold mb-4">
                        Download Loved Tracks
                    </h2>
                    <p className="text-neutral-300 mb-8 text-sm sm:text-base leading-relaxed">
                        Download your favorite tracks from Last.fm and sync them with BeatBridge.
                        Keep all your musical gems in one place.
                    </p>

                    <label className="block mb-2 text-sm text-neutral-400">
                        Enter Your Last.fm username
                    </label>
                    <div className="flex flex-col sm:flex-row gap-3">
                        <UsernameInput
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            placeholder="User name"
                        />
                        <PanelButton onClick={handleDownload}>
                            Download loved tracks
                        </PanelButton>
                    </div>

                    {isLoading ? (
                        <p className="text-gray-300 text-base mt-4">
                            Fetching loved tracks...
                        </p>
                    ) : savedAs ? (
                        <p className="text-gray-300 text-base mt-4">
                            Saved as:{" "}
                            <span className="font-bold text-white">"{savedAs}"</span>
                        </p>
                    ) : null}

                    <p className="mt-4 text-sm text-neutral-400">
                        Don't have a Last.fm account?{" "}
                        <a
                            href="https://www.last.fm"
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-[#1DB954] hover:text-white"
                        >
                            Create one here
                        </a>
                    </p>
                </section>

                <section className="w-full max-w-[44rem] mx-auto flex flex-col items-center justify-center text-center space-y-6">
                    <div className="w-32 h-32 sm:w-40 sm:h-40 md:w-48 md:h-48 bg-transparent rounded-full flex items-center justify-center border-2 border-[#1DB954]">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            fill="currentColor"
                            viewBox="0 0 24 24"
                            className="w-20 h-20 sm:w-24 sm:h-24 text-[#1DB954]"
                        >
                            <path d="M8 5v14l11-7z" />
                        </svg>
                    </div>

                    <p className="text-neutral-300 text-base sm:text-lg">
                        Already <span className="text-[#1DB954] font-semibold">{stats.totalTracks.toLocaleString()}</span>{" "}
                        tracks downloaded by users
                    </p>

                    <div className="flex items-center gap-8">
                        <div className="text-center">
                            <div className="text-2xl sm:text-3xl font-bold text-[#1DB954]">
                                {Math.round(stats.averageTracksPerUser)}
                            </div>
                            <div className="text-sm text-neutral-400">
                                Average number of fetched loved tracks
                            </div>
                        </div>
                        <div className="text-center">
                            <div className="text-2xl sm:text-3xl font-bold text-[#1DB954]">
                                {stats.totalLinkedAccounts}
                            </div>
                            <div className="text-sm text-neutral-400">
                                Total number of linked Spotify and Last.fm accounts
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    );
}