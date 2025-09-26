import React, { useState } from 'react';
import UsernameInput from '../components/UsernameInput';
import PanelButton from "../components/PanelButton.jsx";

export default function LovedTracksPanel() {
    const [username, setUsername] = useState('');
    const [savedAs, setSavedAs] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleDownload = async () => {
        if (!username.trim()) {
            alert("Please enter your Last.fm username.");
            return;
        }

        setIsLoading(true);
        setSavedAs("");

        try {
            const response = await fetch(`/musicapp/user-tracks/import?username=${username}`, {
                method: 'GET',
            });

            if (response.status === 204) {
                alert(`User "${username}" exists but has no loved tracks.`);
            } else if (response.status === 404) {
                alert(`User "${username}" was not found on Last.fm.`);
            } else {
                const text = await response.text();
                alert(`Unexpected error: ${text}`);
            }
        } catch (err) {
            console.error("Error while fetching loved tracks:", err);
            alert("Error while fetching loved tracks");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full h-full flex items-center justify-center px-4">
            <div className="w-full h-[70%] max-w-[64rem] bg-[#2a2a2a] border border-gray-500 rounded-xl shadow-md p-6
                sm:p-10 md:p-14 lg:p-16 flex flex-col justify-evenly gap-6">

                <h2 className="text-white text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-extrabold text-center leading-snug">
                    Download Loved Tracks
                </h2>

                <div className="flex flex-col gap-4">
                    <label className="text-gray-300 text-xl">Enter Last.fm username</label>
                    <div className="w-full flex flex-col sm:flex-row gap-4">
                        <div className="flex-1">
                            <UsernameInput
                                value={username}
                                onChange={e => setUsername(e.target.value)}
                                placeholder={"User name"}
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            <PanelButton onClick={handleDownload}>
                                Download loved tracks
                            </PanelButton>
                        </div>
                    </div>

                    {isLoading ? (
                        <p className="text-gray-300 text-xl">Fetching loved tracks...</p>
                    ) : savedAs ? (
                        <p className="text-gray-300 text-xl">
                            Saved as: <span className="font-bold text-white">"{savedAs}"</span>
                        </p>
                    ) : null}
                </div>

            </div>
        </div>
    );
}