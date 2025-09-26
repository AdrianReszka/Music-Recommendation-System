import React, { useEffect, useState } from "react";
import DropdownSelect from "../components/DropdownSelect.jsx";
import PanelButton from "../components/PanelButton.jsx";

export default function PlaylistsPanel() {
    const [usernames, setUsernames] = useState([]);
    const [selectedList, setSelectedList] = useState("");
    const [selectedTracks, setSelectedTracks] = useState([]);
    const [tracks, setTracks] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [saved, setSaved] = useState(false);

    const fixedPlaylistName = "BeatBridge Recommendations Playlist";

    useEffect(() => {
        const fetchUsernames = async () => {
            try {
                const res = await fetch("/musicapp/recommendations/users");
                const data = await res.json();
                setUsernames(data);
            } catch (err) {
                console.error("Failed to fetch usernames", err);
            }
        };
        fetchUsernames();
    }, []);

    const handleListChange = async (listName) => {
        setSelectedList(listName);
        setSelectedTracks([]);
        setSaved(false);

        const username = listName.replace("Recommended tracks for ", "");

        try {
            const res = await fetch(`/musicapp/recommendations/user/${username}`);
            if (res.ok) {
                const data = await res.json();
                setTracks(data);
            } else {
                const text = await res.text();
                console.error("Failed to fetch recommendations:", text);
                setTracks([]);
            }
        } catch (err) {
            console.error("Error fetching tracks", err);
            setTracks([]);
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

        const spotifyId = localStorage.getItem("spotify_id");
        if (!spotifyId) {
            alert("Spotify user not logged in");
            return;
        }

        const selectedDtos = tracks
            .filter(track => selectedTracks.includes(track.id))
            .map(track => ({
                title: track.title,
                artist: track.artist
            }));

        if (selectedDtos.length === 0) {
            alert("Please select at least one track");
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
        <div className="w-full h-full flex items-center justify-center px-4">
            <div className="w-full h-[70%] max-w-[64rem] bg-[#2a2a2a] border border-gray-500 rounded-xl shadow-md
                            p-6 sm:p-10 md:p-14 lg:p-16 flex flex-col justify-evenly">

                <h2 className="text-white text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-extrabold text-center leading-snug">
                    Save Playlist
                </h2>

                <div className="flex flex-col gap-4">
                    <label className="text-gray-300 text-xl">Select recommendations list</label>
                    <div className="w-full flex flex-col sm:flex-row gap-4">
                        <div className="flex-1">
                            <DropdownSelect
                                options={usernames.map(u => `Recommended tracks for ${u}`)}
                                placeholder="Choose a list"
                                value={selectedList}
                                onChange={handleListChange}
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            <PanelButton onClick={handleSavePlaylist}>
                                Save Playlist
                            </PanelButton>
                        </div>
                    </div>

                    {isLoading ? (
                        <p className="text-gray-300 text-xl">Saving playlist...</p>
                    ) : saved ? (
                        <p className="text-gray-300 text-xl">
                            Saved as: <span className="font-bold text-white">"{fixedPlaylistName}"</span>
                        </p>
                    ) : null}
                </div>

                {tracks.length > 0 && (
                    <div className="flex flex-col gap-2 max-h-60 overflow-y-auto pr-2 hide-scrollbar">
                        {tracks.map((track, idx) => (
                            <label
                                key={idx}
                                className="flex items-center gap-4 px-4 py-3 rounded-2xl cursor-pointer shadow-md
                                           bg-[#1a1a1a] border border-transparent hover:bg-[#444] hover:border-white
                                           transition focus:outline-none"
                            >
                                <input
                                    type="checkbox"
                                    checked={selectedTracks.includes(track.id)}
                                    onChange={() => toggleTrack(track.id)}
                                    className="w-5 h-5 accent-white"
                                />
                                <div className="flex flex-col">
                                    <span className="text-white text-lg font-bold">{track.title}</span>
                                    <span className="text-gray-400 text-sm">{track.artist}</span>
                                </div>
                            </label>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
