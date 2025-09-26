import * as React from 'react';

function SidebarButton({ text, onClick, isLogout = false }) {
    return (
        <button
            onClick={onClick}
            onMouseUp={(e) => e.currentTarget.blur()}
            className={`w-4/5 py-6 text-[1.25rem] font-bold rounded-2xl transition whitespace-nowrap flex items-center justify-center shadow-md
                border border-transparent hover:border-white focus:border-white focus:outline-none
                ${isLogout
                ? 'bg-[#1a1a1a] text-red-400 hover:bg-red-500 hover:text-white'
                : 'bg-[#1a1a1a] hover:bg-[#444] text-white'
            }`}
        >
            {text}
        </button>
    );
}


export default SidebarButton;
