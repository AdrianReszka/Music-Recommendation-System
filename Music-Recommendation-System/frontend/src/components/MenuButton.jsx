import * as React from 'react';

function MenuButton({ label, onClick }) {
    return (
        <button
            onClick={onClick}
            className="text-white font-bold rounded-[10px] border-2 border-transparent
                       hover:border-white focus:outline-none
                       transition duration-200"
            style={{
                width: 'clamp(400px, 50vw, 400px)',
                padding: 'clamp(0.9rem, 1.8vh, 1.1rem) clamp(1.8rem, 3vw, 2.2rem)',
                fontSize: 'clamp(1.05rem, 1.8vw, 1.15rem)',
                backgroundColor: '#353535',
                boxShadow: '0 2px 6px rgba(0,0,0,0.4)',
            }}
            onMouseEnter={e => {
                e.currentTarget.style.border = '1px solid white';
            }}
            onMouseLeave={e => {
                e.currentTarget.style.border = '1px solid transparent';
            }}
            onFocus={e => {
                e.currentTarget.style.outline = '1px solid white';
            }}
            onBlur={e => {
                e.currentTarget.style.outline = 'none';
            }}
        >
            {label}
        </button>
    );
}

export default MenuButton;
