import { createContext, useContext } from 'react';
import useSocket from './sockethandler';
const SocketContext = createContext(null);

export function SocketProvider({ children }) {
    const { socket, isConnected } = useSocket('http://localhost:3000');

    return (
        <SocketContext.Provider value={{ socket, isConnected }}>
            {children}
        </SocketContext.Provider>
    );
}
export const useSocketContext = () => useContext(SocketContext);