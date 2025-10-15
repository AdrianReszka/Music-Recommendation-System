import React from 'react';

export default function DimOverlay({ visible, onClick, zIndex = 1000 }) {
    if (!visible) return null;

    return (
        <div
            onClick={onClick}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm transition-opacity duration-300"
            style={{ zIndex }}
        />
    );
}