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

    const handleListChange = (listName) => {
        const spotifyId = sessionStorage.getItem("spotify_id");
        if (!spotifyId) {
            console.warn("No Spotify user logged in");
            return;
        }

        setSelectedList(listName);
        setRecommendations([]);
        setSelectedTracks([]);
        setCreatedFrom("");

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

        fetch(endpoint)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch tracks");
                return res.json();
            })
            .then((data) => setRecommendations(data))
            .catch((err) => {
                console.error("Failed to fetch tracks", err);
                setRecommendations([]);
            });
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
                await res.json();
                setCreatedFrom(username);
            } else {
                const err = await res.text();
                alert("Backend error: " + err);
            }
        } catch (err) {
            alert("Failed to generate recommendations");
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
                            options={users.map(u => `${u} loved tracks`)}
                            placeholder="Choose a list"
                            value={selectedList}
                            onChange={handleListChange}
                        />
                    </div>

                    <PanelButton
                        onMouseDown={() => document.activeElement.blur()}
                        onClick={handleGenerate}
                    >
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
                    <div className="mt-8 grid sm:grid-cols-2 gap-3 max-h-72 overflow-y-auto pr-2 hide-scrollbar">
                        {recommendations.map((track, idx) => (
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
                                    className="text-[#1DB954] hover:text-white cursor-pointer transition transform hover:scale-110"
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
                                        console.log(`Remove ${track.title}`);
                                    }}
                                    className="text-gray-400 hover:text-red-500 cursor-pointer transition transform hover:scale-110 ml-1"
                                    title="Remove track"
                                >
                                    <svg
                                        xmlns="http://www.w3.org/2000/svg"
                                        fill="none"
                                        viewBox="0 0 24 24"
                                        strokeWidth="2"
                                        stroke="currentColor"
                                        className="w-5 h-5"
                                    >
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </label>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}
