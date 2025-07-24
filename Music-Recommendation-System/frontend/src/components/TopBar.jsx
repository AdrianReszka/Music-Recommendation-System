import * as React from 'react';

function TopBar({ username }) {
    return (
        <div className="fixed top-0 left-0 w-full h-[10vh] bg-[#353535] flex justify-between items-center px-8 text-white text-[1.25rem] font-bold z-10 shadow-md">
            <div>BeatBridge</div>
            <div>Hello, {username}!</div>
        </div>
    );
}

export default TopBar;
