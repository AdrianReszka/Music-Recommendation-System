import React, { createContext, useContext } from 'react';

const UIContext = createContext({
    menuOpen: false,
    setMenuOpen: () => {},
});

export const UIProvider = ({ menuOpen, setMenuOpen, children }) => (
    <UIContext.Provider value={{ menuOpen, setMenuOpen }}>
        {children}
    </UIContext.Provider>
);

export const useUI = () => useContext(UIContext);