import React, { useState, useEffect } from 'react';
import PanelButton from "../components/PanelButton.jsx";
import DropdownSelect from "../components/DropdownSelect.jsx";

export default function RecommendationsPanel() {
    const [users, setUsers] = useState([]);
    const [selectedList, setSelectedList] = useState('');
    const [recommendations, setRecommendations] = useState([]);
    const [selectedTracks, setSelectedTracks] = useState([]);
    const [createdFrom, setCreatedFrom] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        const fetchLinkedUsers = async () => {
            const spotifyId = sessionStorage.getItem("spotify_id");
            if (!spotifyId) {
                console.warn("No Spotify user logged in");
                setUsers([]);
                return;
            }

            try {
                const res = await fetch(`/musicapp/users?spotifyId=${spotifyId}`);
                if (!res.ok) {
                    console.error("Failed to fetch linked users");
                    setUsers([]);
                    return;
                }

                const data = await res.json();
                setUsers(data.map(u => u.lastfmUsername));
            } catch (err) {
                console.error("Failed to fetch users", err);
                setUsers([]);
            }
        };

        fetchLinkedUsers();
    }, []);

    const handleListChange = async (listName) => {
        const spotifyId = sessionStorage.getItem("spotify_id");
        setSelectedList(listName);

        const username = listName
            .replace(" loved tracks", "")
            .replace("Recommended tracks for ", "");

        let endpoint = "";
        if (listName.includes("loved tracks")) {
            endpoint = `/musicapp/user-tracks/${username}?spotifyId=${spotifyId}`;
        } else if (listName.includes("Recommended tracks")) {
            endpoint = `/musicapp/recommendations/user/${username}?spotifyId=${spotifyId}`;
        } else {
            console.warn("Unknown list selected");
            return;
        }

        try {
            const res = await fetch(endpoint);
            if (res.ok) {
                const data = await res.json();
                setRecommendations(data);
            } else {
                console.error("Failed to fetch tracks, status:", res.status);
                setRecommendations([]);
            }
        } catch (err) {
            console.error("Failed to fetch tracks", err);
            setRecommendations([]);
        }

        setSelectedTracks([]);
        setCreatedFrom("");
    };

    const toggleTrack = (trackId) => {
        setSelectedTracks(prev =>
            prev.includes(trackId)
                ? prev.filter(id => id !== trackId)
                : [...prev, trackId]
        );
    };

    const handleGenerate = async () => {
        if (!selectedList) {
            alert("Please select a list before generating recommendations.");
            return;
        }
        if (selectedTracks.length === 0) {
            alert("Please select at least one track.");
            return;
        }

        const spotifyId = sessionStorage.getItem("spotify_id");
        const username = selectedList.replace(" loved tracks", "");

        setRecommendations([]);
        setSelectedTracks([]);
        setCreatedFrom("");
        setIsLoading(true);

        try {
            const res = await fetch(`/musicapp/lastfm/similar?username=${username}&spotifyId=${spotifyId}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(selectedTracks)
            });
            if (res.ok) {
                const data = await res.json();
                setCreatedFrom(username);
            } else {
                const err = await res.text();
                console.error("Failed to generate recommendations:", err);
                alert("Backend error: " + err);
            }
        } catch (err) {
            console.error("Error generating recommendations", err);
            alert("Failed to generate recommendations");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full h-full flex items-center justify-center px-4">
            <div className="w-full h-[70%] max-w-[64rem] bg-[#2a2a2a] border border-gray-500 rounded-xl shadow-md p-6 sm:p-10
                md:p-14 lg:p-16 flex flex-col justify-evenly gap-6">

                <h2 className="text-white text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-extrabold text-center leading-snug">
                    Generate Recommendations
                </h2>

                <div className="flex flex-col gap-4">
                    <label className="text-gray-300 text-xl">Select loved tracks list</label>
                    <div className="w-full flex flex-col sm:flex-row gap-4">
                        <div className="flex-1">
                            <DropdownSelect
                                options={users.map(u => `${u} loved tracks`)}
                                placeholder="Choose a list"
                                value={selectedList}
                                onChange={handleListChange}
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            <PanelButton onClick={handleGenerate}>
                                Generate recommendations
                            </PanelButton>
                        </div>
                    </div>

                    {isLoading ? (
                        <p className="text-gray-300 text-xl">Generating recommendations...</p>
                    ) : createdFrom ? (
                        <p className="text-gray-300 text-xl">
                            Saved as: <span className="font-bold text-white">"Recommended tracks for {createdFrom}"</span>
                        </p>
                    ) : null}
                </div>

                {recommendations.length > 0 && (
                    <div className="flex flex-col gap-2 max-h-60 overflow-y-auto pr-2 hide-scrollbar">
                        {recommendations.map((track, idx) => (
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