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
    const [isLoadingTracks, setIsLoadingTracks] = useState(false);

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

        setSaved(false);
        setIsLoadingTracks(true);

        if (selectedList?.label === label) {
            setSelectedList(null);
            await new Promise(r => setTimeout(r, 0));
            setSelectedList(selected);
        } else {
            setSelectedList(selected);
        }

        setTracks([]);
        setSelectedTracks([]);

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
            setIsLoadingTracks(false);
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
            alert("Spotify user not logged in. Please log in to save playlists.");
            return;
        }

        const selectedDtos = tracks
            .filter(track => selectedTracks.includes(track.id))
            .map(track => ({ title: track.title, artist: track.artist }));

        if (selectedDtos.length === 0) {
            alert("Please select at least one track.");
            return;
        }

        setTracks([]);
        setSelectedTracks([]);
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

                <p className="text-gray-300 mb-8 text-sm sm:text-base leading-relaxed">
                    Choose one of your generated recommendations lists and save it as a playlist directly on Spotify.
                </p>

                <label className="block mb-2 text-sm text-gray-400">
                    Select recommendations list
                </label>

                <div className="flex flex-col sm:flex-row gap-3">
                    <div className="flex-1">
                        <DropdownSelect
                            key={lists.map(l => l.batchId).join(",")}
                            options={lists.map(l => l.label)}
                            placeholder="Choose a list"
                            value={selectedList?.label || ""}
                            onChange={handleListChange}
                            onDelete={async (option) => {
                                const selected = lists.find(l => l.label === option);
                                if (!selected) return;

                                const confirmDelete = confirm(
                                    `Delete all recommendations for ${selected.username}?`
                                );
                                if (!confirmDelete) return;

                                try {
                                    const res = await fetch(
                                        `/musicapp/recommendations/user/${selected.username}/batch/${selected.batchId}`,
                                        { method: "DELETE" }
                                    );

                                    if (res.ok || res.status === 204) {
                                        alert(
                                            `All recommendations for ${selected.username} were deleted.`
                                        );
                                        setLists(prev => prev.filter(l => l.batchId !== selected.batchId));
                                        setSelectedList(null);
                                        setTracks([]);
                                    } else {
                                        const err = await res.text();
                                        console.error("Failed to delete recommendation batch:", err);
                                        alert("Failed to delete recommendation batch.");
                                    }
                                } catch (err) {
                                    console.error("Error deleting recommendation batch:", err);
                                    alert("Error deleting recommendation batch.");
                                }
                            }}
                        />
                    </div>
                    <PanelButton
                        onMouseDown={() => document.activeElement.blur()}
                        onClick={handleSavePlaylist}
                    >
                        Save playlist
                    </PanelButton>
                </div>

                {isLoadingTracks && (
                    <p className="text-gray-300 text-base mt-4">Loading tracks...</p>
                )}

                {isLoading ? (
                    <p className="text-gray-300 text-base mt-4">
                        Saving playlist...
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
                    <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-72 overflow-y-auto pr-2 hide-scrollbar">
                        {tracks.map((track, idx) => (
                            <label
                                key={idx}
                                className="flex items-center gap-3 p-3 rounded-xl bg-[#1f1f1f] hover:bg-[#262626]
                                       transition cursor-pointer shadow-md relative group"
                            >
                                <input
                                    type="checkbox"
                                    checked={selectedTracks.includes(track.id)}
                                    onChange={() => toggleTrack(track.id)}
                                    className="w-5 h-5 accent-[#1DB954] flex-shrink-0"
                                />

                                <div className="flex flex-col overflow-hidden min-w-0 flex-1">
                                    <span className="text-white font-semibold truncate">{track.title}</span>
                                    <span className="text-gray-400 text-sm truncate">{track.artist}</span>
                                </div>

                                <button
                                    onClick={(e) => {
                                        e.preventDefault();

                                        if (!track.spotifyId) {
                                            alert("No Spotify link available for this track.");
                                            return;
                                        }

                                        const cleanId = track.spotifyId.includes(":")
                                            ? track.spotifyId.split(":").pop()
                                            : track.spotifyId;

                                        const spotifyUrl = `https://open.spotify.com/track/${cleanId}`;
                                        window.open(spotifyUrl, "_blank");
                                    }}
                                    className="text-[#1DB954] hover:text-[#1ED760] cursor-pointer transition text-lg flex-shrink-0"
                                    title="Open on Spotify"
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
                                    onClick={async (e) => {
                                        e.preventDefault();
                                        if (!selectedList) return;

                                        const username = selectedList.username;
                                        const batchId = selectedList.batchId;

                                        try {
                                            const res = await fetch(`/musicapp/recommendations/user/${username}/batch/${batchId}/track/${track.id}`, {
                                                method: "DELETE",
                                            });

                                            if (res.ok || res.status === 204) {
                                                setTracks(prev => {
                                                    const updated = prev.filter(t => t.id !== track.id);
                                                    setSelectedTracks(st => st.filter(id => id !== track.id));

                                                    if (updated.length === 0) {
                                                        fetch(`/musicapp/recommendations/user/${username}/batch/${batchId}`, {
                                                            method: "DELETE",
                                                        }).then(() => {
                                                            setLists(prev => prev.filter(l => l.batchId !== batchId));
                                                            setSelectedList(null);
                                                            setTracks([]);
                                                            setSelectedTracks([]);
                                                        }).catch(err => console.error("Error deleting empty batch:", err));
                                                    }

                                                    return updated;
                                                });
                                            } else {
                                                const err = await res.text();
                                                console.error("Failed to delete recommendation:", err);
                                            }
                                        } catch (err) {
                                            console.error("Error deleting recommendation:", err);
                                        }
                                    }}
                                    className="text-gray-400 hover:text-red-500 cursor-pointer transition text-lg font-bold ml-2 flex-shrink-0"
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
