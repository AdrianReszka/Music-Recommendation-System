import React from 'react';

export default function UsernameInput({ value, onChange, placeholder}) {
    return (
        <input
            type="text"
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className="bg-zinc-800 text-white border border-gray-500 rounded px-4 py-3 w-full text-xl
                       placeholder-gray-500 focus:border-white outline-none"
        />
    );
}
