import React, { useEffect, useRef, useState } from "react";
import { ChevronDown } from "lucide-react";

export default function DropdownSelect({ options, placeholder, value, onChange, onDelete, showDelete = true }) {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
                dropdownRef.current.querySelector("div").blur();
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleSelect = (option) => {
        onChange(option);
        setIsOpen(false);
    };

    const handleDelete = (e, option) => {
        e.stopPropagation();
        if (onDelete) onDelete(option);
    };

    return (
        <div className="relative w-full" ref={dropdownRef}>
            <div
                tabIndex={0}
                onClick={() => setIsOpen(!isOpen)}
                className="relative bg-[#1f1f1f] border border-white rounded px-4 py-3 text-xl cursor-pointer select-none
                   focus:outline-none hover:ring-2 hover:ring-white focus:ring-2 focus:ring-white transition-all duration-200"
            >
                {value ? (
                    <span className="text-white">{value}</span>
                ) : (
                    <span className="text-gray-500">{placeholder}</span>
                )}

                <ChevronDown
                    className={`absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 text-white transition-transform duration-200
                               ${isOpen ? "rotate-180" : ""}`}
                />
            </div>

            {isOpen && (
                <ul
                    className="absolute mt-1 w-full bg-[#1f1f1f] border border-white rounded shadow-lg
                               max-h-60 overflow-y-auto z-50 hide-scrollbar"
                >
                    {options.length === 0 ? (
                        <li className="px-4 py-2 text-gray-500 text-lg">
                            No options available
                        </li>
                    ) : (
                        options.map((option, index) => (
                            <li
                                key={index}
                                onClick={() => handleSelect(option)}
                                className={`flex items-center justify-between px-4 py-2 text-lg text-white cursor-pointer
                                           hover:bg-[#2a2a2a] transition-all duration-200 focus:outline-none
                                           ${index !== options.length - 1 ? 'border-b border-white' : ''}
                                           ${index !== options.length - 1 ? 'hover:border-white' : ''}`}
                            >
                                <span className="truncate">{option}</span>

                                {showDelete && (
                                    <button
                                        onClick={(e) => handleDelete(e, option)}
                                        className="text-white hover:text-red-500 cursor-pointer transition text-lg font-bold ml-3 flex-shrink-0"
                                        title="Remove option"
                                    >
                                        ✕
                                    </button>
                                )}
                            </li>
                        ))
                    )}
                </ul>
            )}
        </div>
    );
}
