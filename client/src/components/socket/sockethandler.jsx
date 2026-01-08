import { useEffect, useRef, useState } from 'react';
import { io } from 'socket.io-client';

export default function useSocket(url) {
    const socket = useRef(null);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        socket.current = io(url);

        socket.current.on('connect', () => {
            console.log('Connected to server');
            setIsConnected(true);
        });

        socket.current.on('disconnect', () => {
            console.log('Disconnected from server');
            setIsConnected(false);
        });

        return () => {
            socket.current.disconnect();
        };
    }, [url]);
    return { socket: socket.current, isConnected };
}