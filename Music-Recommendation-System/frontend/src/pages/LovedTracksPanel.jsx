import React, { useState } from 'react';
import UsernameInput from '../components/UsernameInput';
import DownloadButton from '../components/DownloadButton';
import StatusMessage from '../components/StatusMessage';

export default function LovedTracksPanel() {
    const [username, setUsername] = useState('');
    const [savedAs, setSavedAs] = useState('');

    const handleDownload = async () => {
        if (!username.trim()) {
            alert("Please enter your Last.fm username.");
            return;
        }

        try {
            const response = await fetch(`musicapp/lastfm/loved?username=${username}`, {
                method: 'GET',
            });
            if (response.ok) {
                setSavedAs(`${username} loved tracks`);
            } else {
                const text = await response.text();
                alert(`Failed to fetch loved tracks: ${text}`);
            }
        } catch (err) {
            console.error("Error while fetching loved tracks:", err);
            alert("Error while fetching loved tracks");
        }
    };

    return (
        <div className="w-full h-full flex items-center justify-center px-4">
            <div className="w-full h-[70%] max-w-[64rem] bg-[#2a2a2a] border border-gray-500 rounded-xl shadow-md p-6 sm:p-10 md:p-14 lg:p-16 flex flex-col justify-evenly">

                <h2 className="text-white text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-extrabold text-center leading-snug">
                    Download Loved Tracks
                </h2>

                <div className="flex flex-col gap-4">
                    <label className="text-gray-300 text-xl">Your Last.fm username</label>
                    <div className="w-full flex flex-col sm:flex-row gap-4">
                        <div className="flex-1 ">
                            <UsernameInput value={username} onChange={e => setUsername(e.target.value)} />
                        </div>
                        <div className="w-full sm:w-auto">
                            <DownloadButton onClick={handleDownload} />
                        </div>
                    </div>

                    {savedAs && (
                        <p className="text-gray-300 gap-4 text-xl">Saved as: {savedAs}</p>
                    )}
                </div>

            </div>
        </div>
    );
}
