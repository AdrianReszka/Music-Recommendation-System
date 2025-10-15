export default function PanelButton({ onClick, children }) {
    return (
        <button
            onClick={onClick}
            onMouseUp={(e) => e.currentTarget.blur()}
            className="w-full sm:w-auto text-lg font-bold rounded-2xl transition whitespace-nowrap flex items-center justify-center shadow-md
                       px-6 py-3 bg-[#1DB954] hover:bg-[#1DB954]/60 text-[#191414] cursor-pointer
                       border border-transparent hover:border-white focus:outline-none focus:ring-2 focus:ring-white"
        >
            {children}
        </button>
    );
}
