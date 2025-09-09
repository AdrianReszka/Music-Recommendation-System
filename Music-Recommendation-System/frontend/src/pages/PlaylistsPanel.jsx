import React, { useState } from "react";
import DropdownSelect from "../components/DropdownSelect.jsx";
import PanelButton from "../components/PanelButton.jsx";
import UsernameInput from "../components/UsernameInput.jsx";

export default function PlaylistsPanel() {
    const [selectedList, setSelectedList] = useState("");
    const [playlistName, setPlaylistName] = useState("");
    const [savedAs, setSavedAs] = useState(""); // nowy state

    const handleSave = () => {
        if (!selectedList || !playlistName.trim()) {
            alert("Please select a list and enter a playlist name.");
            return;
        }
        console.log("Saving playlist:", playlistName, "from list:", selectedList);
        setSavedAs(playlistName.trim());
    };

    return (
        <div className="w-full h-full flex items-center justify-center px-4">
            <div className="w-full h-[70%] max-w-[64rem] bg-[#2a2a2a] border border-gray-500 rounded-xl shadow-md
                            p-6 sm:p-10 md:p-14 lg:p-16 flex flex-col justify-evenly gap-6">

                <h2 className="text-white text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-extrabold text-center leading-snug">
                    Save Playlist
                </h2>

                <div className="flex flex-col gap-4">
                    <label className="text-gray-300 text-xl">Enter playlist name</label>
                    <div className="flex flex-col sm:flex-row gap-4">
                        <div className="flex-1">
                            <UsernameInput
                                value={playlistName}
                                onChange={(e) => setPlaylistName(e.target.value)}
                                placeholder="Playlist name"
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            <PanelButton onClick={handleSave}>
                                Save Playlist
                            </PanelButton>
                        </div>
                    </div>

                    {savedAs && (
                        <p className="text-gray-300 text-xl">
                            Playlist saved as: <span className="font-bold text-white">"{savedAs}"</span>
                        </p>
                    )}
                </div>

                <div className="flex flex-col gap-4">
                    <label className="text-gray-300 text-xl">Select recommendations list</label>
                    <DropdownSelect
                        options={[
                            "Adicom loved tracks",
                            "Ziomeczek loved tracks",
                            "Recommended tracks for Ziomeczek",
                            "Recommended tracks for Adicom"
                        ]}
                        placeholder="Choose a list"
                        value={selectedList}
                        onChange={setSelectedList}
                    />
                </div>
            </div>
        </div>
    );
}
