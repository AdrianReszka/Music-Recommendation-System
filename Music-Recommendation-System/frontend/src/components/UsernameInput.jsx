import React from 'react';

export default function UsernameInput({ value, onChange }) {
    return (
        <input
            type="text"
            value={value}
            onChange={onChange}
            placeholder="User name"
            className="bg-zinc-800 text-white border border-gray-500 rounded px-4 py-3 w-full text-xl"
        />

    );
}
