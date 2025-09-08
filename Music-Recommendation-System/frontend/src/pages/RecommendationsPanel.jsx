import React, { useState } from 'react';
import PanelButton from "../components/PanelButton.jsx";
import DropdownSelect from "../components/DropdownSelect.jsx";

export default function RecommendationsPanel() {
    const [selectedList, setSelectedList] = useState('');
    const [recommendations, setRecommendations] = useState([]);
    const [selectedTracks, setSelectedTracks] = useState([]);
    const [createdFrom, setCreatedFrom] = useState('');

    const handleListChange = (listName) => {
        setSelectedList(listName);

        if (listName === 'Adicom loved tracks') {
            setRecommendations([
                { id: 1, title: "Alex boy", artist: "Actum" },
                { id: 2, title: "Franktors", artist: "Tremple" },
                { id: 3, title: "Chelsea", artist: "Satatica" },
                { id: 4, title: "DJ Sample", artist: "Beat1" },
                { id: 5, title: "MC Test", artist: "Flow2" },
                { id: 6, title: "Band XYZ", artist: "Track3" },
                { id: 7, title: "Artist A", artist: "Song7" },
                { id: 8, title: "Artist B", artist: "Song8" },
                { id: 9, title: "Artist C", artist: "Song9" },
                { id: 10, title: "Artist D", artist: "Song10" },
            ]);
        } else if (listName === 'Ziomeczek loved tracks') {
            setRecommendations([
                { id: 11, title: "Demo Artist", artist: "Alpha" },
                { id: 12, title: "Demo Artist", artist: "Beta" },
                { id: 13, title: "Demo Artist", artist: "Gamma" },
            ]);
        } else {
            setRecommendations([]);
        }

        setSelectedTracks([]);
    };

    const toggleTrack = (id) => {
        setSelectedTracks(prev =>
            prev.includes(id)
                ? prev.filter(tid => tid !== id)
                : [...prev, id]
        );
    };

    return (
        <div className="w-full h-full flex items-center justify-center px-4">
            <div className="w-full h-[70%] max-w-[64rem] bg-[#2a2a2a] border border-gray-500 rounded-xl shadow-md p-6 sm:p-10 md:p-14 lg:p-16 flex flex-col justify-evenly gap-6">

                <h2 className="text-white text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-extrabold text-center leading-snug">
                    Generate Recommendations
                </h2>

                <div className="flex flex-col gap-4">
                    <label className="text-gray-300 text-xl">Select loved tracks list</label>
                    <div className="w-full flex flex-col sm:flex-row gap-4">
                        <div className="flex-1">
                            <DropdownSelect
                                options={["Adicom loved tracks", "Ziomeczek loved tracks"]}
                                placeholder="Choose a list"
                                value={selectedList}
                                onChange={handleListChange}
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            <PanelButton onClick={() => setCreatedFrom(selectedList)}>
                                Generate recommendations
                            </PanelButton>
                        </div>
                    </div>

                    {createdFrom && (
                        <p className="text-gray-300 text-xl">
                            Created recommendations based on: {createdFrom}
                        </p>
                    )}
                </div>

                {recommendations.length > 0 && (
                    <div className="flex flex-col gap-2 max-h-60 overflow-y-auto pr-2 hide-scrollbar">
                        {recommendations.map(track => (
                            <label
                                key={track.id}
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

