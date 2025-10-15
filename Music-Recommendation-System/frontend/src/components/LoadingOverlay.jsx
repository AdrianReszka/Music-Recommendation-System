import React, { useEffect } from 'react';
import { useUI } from '../context/UIContext.jsx';

export default function LoadingOverlay({ visible, text = "Loading...", zIndex = 1200 }) {
    const { menuOpen, setMenuOpen } = useUI();

    useEffect(() => {
        if (visible) setMenuOpen(false);
    }, [visible, setMenuOpen]);

    if (!visible || menuOpen) return null;

    return (
        <div
            className="fixed inset-0 flex items-center justify-center bg-black/60 transition-opacity duration-300"
            style={{ zIndex }}
        >
            <div className="flex items-center gap-4 px-6 py-4 rounded-2xl bg-[#1f1f1f] shadow-xl border border-white/10">
                <div className="relative">
                    <div className="h-10 w-10 rounded-full border-4 border-white/10 border-t-[#1DB954] animate-spin"></div>
                    <div className="absolute inset-0 rounded-full border-4 border-transparent border-l-[#17a34a] animate-spin [animation-duration:2s]"></div>
                </div>
                <div className="text-white text-base">{text}</div>
            </div>
        </div>
    );
}
