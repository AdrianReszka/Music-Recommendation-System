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
                console.log("Linked users (raw):", data);
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

        console.log("Selected list name:", listName);

        const username = listName
            .replace(" loved tracks", "")
            .replace("Recommended tracks for ", "");

        console.log("Extracted username:", username);

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
        <div className="w-full min-h-[100vh] flex items-center justify-center px-6">

            <section className="w-full max-w-[44rem] mx-auto text-center md:text-left">

                <h2 className="text-3xl sm:text-4xl md:text-5xl font-extrabold mb-4">
                    Generate Recommendations
                </h2>

                <p className="text-neutral-300 mb-8 text-sm sm:text-base leading-relaxed">
                    Choose a list of loved tracks and generate new song suggestions based on them.
                </p>

                <label className="block mb-2 text-sm text-neutral-400">
                    Select loved tracks list
                </label>

                <div className="flex flex-col sm:flex-row gap-3">
                    <div className="flex-1">
                        <DropdownSelect
                            options={[
                                ...users.map(u => `${u} loved tracks`),
                                ...users.map(u => `Recommended tracks for ${u}`)
                            ]}
                            placeholder="Choose a list"
                            value={selectedList}
                            onChange={handleListChange}
                        />
                    </div>
                    <PanelButton onClick={handleGenerate}>
                        Generate recommendations
                    </PanelButton>
                </div>

                {isLoading ? (
                    <p className="text-gray-300 text-base mt-4">
                        Generating recommendations...
                    </p>
                ) : createdFrom ? (
                    <p className="text-gray-300 text-base mt-4">
                        Saved as:{" "}
                        <span className="font-bold text-white">
                            "Recommended tracks for {createdFrom}"
                        </span>
                    </p>
                ) : null}

                {recommendations.length > 0 && (
                    <div className="mt-6 flex flex-col gap-2 max-h-60 overflow-y-auto pr-2 hide-scrollbar">
                        {recommendations.map((track, idx) => (
                            <label
                                key={idx}
                                className="flex items-center gap-4 px-4 py-3 rounded-2xl cursor-pointer shadow-md
                                           bg-[#1a1a1a] border border-transparent hover:bg-[#333] hover:border-[#1DB954]
                                           transition"
                            >
                                <input
                                    type="checkbox"
                                    checked={selectedTracks.includes(track.id)}
                                    onChange={() => toggleTrack(track.id)}
                                    className="w-5 h-5 accent-[#1DB954]"
                                />
                                <div className="flex flex-col text-left">
                                    <span className="text-white text-lg font-bold">{track.title}</span>
                                    <span className="text-gray-400 text-sm">{track.artist}</span>
                                </div>
                            </label>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}
