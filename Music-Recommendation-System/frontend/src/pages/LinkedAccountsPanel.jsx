import React, { useEffect, useState } from "react";
import DropdownSelect from "../components/DropdownSelect.jsx";
import PanelButton from "../components/PanelButton.jsx";
import LoadingOverlay from "../components/LoadingOverlay.jsx";

export default function LinkedAccountsPanel() {
    const [linkedUsers, setLinkedUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState("");
    const [message, setMessage] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        const fetchLinkedUsers = async () => {
            const spotifyId = sessionStorage.getItem("spotify_id");
            if (!spotifyId) {
                console.warn("No Spotify user logged in");
                alert("Please log in with Spotify to link accounts.");
                return;
            }

            try {
                const res = await fetch(`/musicapp/users?spotifyId=${spotifyId}`);
                if (!res.ok) {
                    console.error("Failed to fetch linked users");
                    alert("Failed to fetch linked users.");
                    return;
                }

                const data = await res.json();
                setLinkedUsers(data.map(u => u.lastfmUsername));
            } catch (err) {
                console.error("Error fetching linked users", err);
                alert("An error occurred while fetching linked users.");
            }
        };

        fetchLinkedUsers();
    }, []);

    const handleUnlink = async (username) => {
        const spotifyId = sessionStorage.getItem("spotify_id");
        if (!spotifyId) return;

        const confirmMsg = confirm(`Do you really want to unlink account "${username}"?`);
        if (!confirmMsg) return;

        setIsLoading(true);
        setMessage("");

        try {
            const res = await fetch(`/musicapp/users/unlink?spotifyId=${spotifyId}&lastfmUsername=${username}`, {
                method: "DELETE",
            });

            if (res.ok) {
                setLinkedUsers(prev => prev.filter(u => u !== username));
                if (selectedUser === username) setSelectedUser("");
                setMessage(
                    <>
                        Account{" "}
                        <span className="font-bold text-white">
                            {username}
                        </span>{" "}
                                    was unlinked successfully
                                </>
                            );
            } else {
                const err = await res.text();
                alert(`Failed to unlink account: ${err}`);
            }
        } catch (err) {
            console.error("Error unlinking account", err);
            alert("An error occurred while unlinking the account.");
        } finally {
            setIsLoading(false);
        }
    };

    const overlayVisible = isLoading;
    const overlayText = "Unlinking account...";

    return (
        <div className="w-full min-h-[100vh] flex items-center justify-center px-6">
            <LoadingOverlay visible={overlayVisible} text={overlayText} />

            <section className="w-full max-w-[44rem] mx-auto text-center md:text-left">
                <h2 className="text-3xl sm:text-4xl md:text-5xl font-extrabold mb-4">
                    Manage Linked Accounts
                </h2>

                <p className="text-gray-300 mb-8 text-sm sm:text-base leading-relaxed">
                    View your connected Last.fm accounts and remove links when needed.
                </p>

                <label className="block mb-2 text-sm text-gray-400">
                    Linked Last.fm accounts
                </label>

                <div className="flex flex-col sm:flex-row gap-3">
                    <div className="flex-1">
                        <DropdownSelect
                            options={linkedUsers}
                            placeholder="Select linked Last.fm account"
                            value={selectedUser}
                            onChange={setSelectedUser}
                            showDelete={false}
                        />
                    </div>

                    <PanelButton
                        onMouseDown={() => document.activeElement.blur()}
                        onClick={() => {
                            if (isLoading) return;
                            if (!selectedUser) {
                                alert("Please select a Last.fm account to unlink.");
                                return;
                            }
                            handleUnlink(selectedUser);
                        }}
                    >
                        Unlink account
                    </PanelButton>
                </div>

                {message && (
                    <p className="mt-4 text-base text-gray-300 transition-all duration-500">
                        {message}
                    </p>
                )}
            </section>
        </div>
    );
}
