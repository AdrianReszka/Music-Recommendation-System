import React, { useEffect, useState } from "react";
import DropdownSelect from "../components/DropdownSelect.jsx";
import PanelButton from "../components/PanelButton.jsx";

export default function PlaylistsPanel() {
    const [lists, setLists] = useState([]);
    const [selectedList, setSelectedList] = useState(null);
    const [selectedTracks, setSelectedTracks] = useState([]);
    const [tracks, setTracks] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [saved, setSaved] = useState(false);

    const fixedPlaylistName = "BeatBridge Recommendations Playlist";

    useEffect(() => {
        const fetchAllRecommendationLists = async () => {
            const spotifyId = sessionStorage.getItem("spotify_id");
            if (!spotifyId) {
                console.warn("No Spotify user logged in — skipping fetch.");
                return;
            }

            try {
                const usersRes = await fetch(`/musicapp/users?spotifyId=${spotifyId}`);
                if (!usersRes.ok) {
                    console.error("Failed to fetch linked users, status:", usersRes.status);
                    return;
                }

                const users = await usersRes.json();
                const allLists = [];

                for (const u of users) {
                    const username = u.lastfmUsername;
                    const res = await fetch(`/musicapp/recommendations/user/${username}?spotifyId=${spotifyId}`);
                    if (res.ok) {
                        const batches = await res.json();
                        batches.forEach(b =>
                            allLists.push({
                                label: `Recommended tracks for ${username} (${b.createdAt})`,
                                username,
                                batchId: b.batchId
                            })
                        );
                    }
                }

                allLists.sort((a, b) => b.label.localeCompare(a.label));
                setLists(allLists);
            } catch (err) {
                console.error("Error fetching recommendation lists:", err);
                setLists([]);
            }
        };

        fetchAllRecommendationLists();
    }, []);

    const handleListChange = async (label) => {
        const selected = lists.find(l => l.label === label);
        const spotifyId = sessionStorage.getItem("spotify_id");
        if (!spotifyId) return;

        if (selectedList?.label === label) {
            setSelectedList(null);
            await new Promise(r => setTimeout(r, 0));
            setSelectedList(selected);
        } else {
            setSelectedList(selected);
        }

        setSelectedList(selected);
        setTracks([]);
        setSelectedTracks([]);
        setSaved(false);
        setIsLoading(true);

        try {
            const res = await fetch(
                `/musicapp/recommendations/user/${selected.username}?spotifyId=${spotifyId}&batchId=${selected.batchId}`
            );
            if (res.ok) {
                const data = await res.json();
                setTracks(data);
            } else {
                const text = await res.text();
                console.error("Failed to fetch tracks:", text);
            }
        } catch (err) {
            console.error("Error fetching tracks:", err);
        } finally {
            setIsLoading(false);
        }
    };

    const toggleTrack = (trackId) => {
        setSelectedTracks(prev =>
            prev.includes(trackId)
                ? prev.filter(id => id !== trackId)
                : [...prev, trackId]
        );
    };

    const handleSavePlaylist = async () => {
        if (!selectedList) {
            alert("Please select a recommendations list first.");
            return;
        }

        const spotifyId = sessionStorage.getItem("spotify_id");
        if (!spotifyId) {
            alert("Spotify user not logged in");
            return;
        }

        const selectedDtos = tracks
            .filter(track => selectedTracks.includes(track.id))
            .map(track => ({ title: track.title, artist: track.artist }));

        if (selectedDtos.length === 0) {
            alert("Please select at least one track");
            return;
        }

        setIsLoading(true);
        setSaved(false);

        try {
            const res = await fetch(`/musicapp/spotify/save-playlist?spotifyId=${spotifyId}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(selectedDtos),
            });

            if (res.ok) {
                setSaved(true);
            } else {
                const err = await res.text();
                alert("Failed to create playlist: " + err);
            }
        } catch (err) {
            console.error("Error creating playlist:", err);
            alert("Error creating playlist");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full min-h-[100vh] flex items-center justify-center px-6">

            <section className="w-full max-w-[44rem] mx-auto text-center md:text-left">

                <h2 className="text-3xl sm:text-4xl md:text-5xl font-extrabold mb-4">
                    Save Playlist
                </h2>

                <p className="text-neutral-300 mb-8 text-sm sm:text-base leading-relaxed">
                    Choose one of your generated recommendation lists and save it as a playlist directly on Spotify.
                </p>

                <label className="block mb-2 text-sm text-neutral-400">
                    Select recommendations list
                </label>

                <div className="flex flex-col sm:flex-row gap-3">
                    <div className="flex-1">
                        <DropdownSelect
                            options={lists.map(l => l.label)}
                            placeholder="Choose a list"
                            value={selectedList?.label || ""}
                            onChange={handleListChange}
                        />
                    </div>
                    <PanelButton
                        onMouseDown={() => document.activeElement.blur()}
                        onClick={handleSavePlaylist}
                    >
                        Save playlist
                    </PanelButton>
                </div>

                {isLoading ? (
                    <p className="text-gray-300 text-base mt-4">
                        {saved ? "Saving playlist..." : "Loading tracks..."}
                    </p>
                ) : saved ? (
                    <p className="text-gray-300 text-base mt-4">
                        Saved as:{" "}
                        <span className="font-bold text-white">
                        "{fixedPlaylistName}"
                    </span>
                    </p>
                ) : null}

                {tracks.length > 0 && (
                    <div className="mt-8 grid sm:grid-cols-2 gap-3 max-h-72 overflow-y-auto pr-2 hide-scrollbar">
                        {tracks.map((track, idx) => (
                            <label
                                key={idx}
                                className="flex items-center gap-3 p-3 rounded-xl bg-[#181818] hover:bg-[#262626]
                                       transition cursor-pointer shadow-md relative group"
                            >
                                <input
                                    type="checkbox"
                                    checked={selectedTracks.includes(track.id)}
                                    onChange={() => toggleTrack(track.id)}
                                    className="w-5 h-5 accent-[#1DB954]"
                                />

                                <div className="flex flex-col overflow-hidden flex-grow">
                                    <span className="text-white font-semibold truncate">{track.title}</span>
                                    <span className="text-gray-400 text-sm truncate">{track.artist}</span>
                                </div>

                                <button
                                    onClick={(e) => {
                                        e.preventDefault();
                                        console.log(`Play preview for ${track.title}`);
                                    }}
                                    className="text-[#1DB954] hover:text-white cursor-pointer transition text-lg"
                                    title="Play preview"
                                >
                                    <svg
                                        xmlns="http://www.w3.org/2000/svg"
                                        fill="currentColor"
                                        viewBox="0 0 24 24"
                                        className="w-6 h-6"
                                    >
                                        <path d="M8 5v14l11-7z" />
                                    </svg>
                                </button>

                                <button
                                    onClick={(e) => {
                                        e.preventDefault();
                                        console.log(`Remove ${track.title} from list`);
                                    }}
                                    className="text-gray-400 hover:text-red-500 cursor-pointer transition text-lg font-bold ml-2"
                                    title="Remove track"
                                >
                                    ✕
                                </button>
                            </label>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}
