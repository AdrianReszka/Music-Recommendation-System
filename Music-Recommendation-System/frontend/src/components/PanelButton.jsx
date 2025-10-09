import React from 'react';

export default function PanelButton({ onClick, children }) {
    return (
        <button
            onClick={onClick}
            onMouseUp={(e) => e.currentTarget.blur()}
            className="w-full sm:w-auto text-lg font-bold rounded-2xl transition whitespace-nowrap flex items-center justify-center shadow-md
                       px-6 py-3 bg-[#1DB954] hover:bg-[#17a34a] text-[#191414] cursor-pointer
                       border border-transparent hover:border-white focus:border-white focus:outline-none"
        >
            {children}
        </button>
    );
}
