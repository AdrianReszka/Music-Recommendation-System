import * as React from 'react';

function SidebarButton({ text, onClick, isLogout = false }) {
    return (
        <button
            onClick={onClick}
            className={`w-4/5 h-2/5 text-[1.25rem] font-bold rounded transition whitespace-nowrap flex items-center justify-center
                ${isLogout
                ? 'bg-[#353535] text-red-400 hover:bg-red-500 hover:text-white'
                : 'bg-[#353535] hover:bg-[#444] text-white'
            }`}
            onMouseEnter={e => {
                e.currentTarget.style.border = '1px solid white';
            }}
            onMouseLeave={e => {
                e.currentTarget.style.border = '1px solid transparent';
            }}
            onBlur={e => {
                e.currentTarget.style.outline = 'none';
            }}
        >
            {text}
        </button>
    );
}

export default SidebarButton;
