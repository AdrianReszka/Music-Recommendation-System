export default function LoginWithSpotifyButton({ onClick }) {
    return (
        <button
            onClick={onClick}
            className="inline-flex items-center justify-center gap-2.5 px-7 py-4 rounded-full text-xl font-semibold shadow-md
             bg-[#1DB954] text-black hover:bg-[#17a34a] active:scale-95
             border border-transparent hover:border-white focus:border-white
             focus:outline-none focus:ring-0 transition-colors my-[5px]"
        >
            <img
                src="/images/custom-icon.png"
                alt="Spotify"
                className="w-7 h-7"
            />
            Log in with Spotify
        </button>

    );
}
