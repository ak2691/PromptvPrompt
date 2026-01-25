
import { useState, useEffect } from 'react';
import { SubmitTurn } from "../service/SubmitTurn";
import fetchGameState from '../service/FetchGameState';
import { useParams, useSearchParams } from 'react-router-dom';
import { useSocketContext } from '../socket/socketcontext';
import SpectatingBanner from '../ui-components/SpectatingBanner';
import TurnInput from '../ui-components/TurnInput';
import ChatMessages from '../ui-components/ChatMessages';
import GameCompleteBanner from '../ui-components/GameCompleteBanner';
import ScoreDisplay from '../ui-components/ScoreDisplay';
import TransitionCountdown from '../ui-components/TransitionCountdown';
export default function GamePage() {
    const [message, setMessage] = useState('');
    const { socket, isConnected } = useSocketContext();
    const MAX_CHARS = 250;
    const [searchParams] = useSearchParams();
    const { gameId } = useParams();
    const [isSpectating, setIsSpectating] = useState(false);
    const userId = searchParams.get('userId');
    const [myMessageCount, setMyMessageCount] = useState(0);
    const [opponentMessageCount, setOpponentMessageCount] = useState(0);
    const [gameComplete, setGameComplete] = useState(false);
    const [winnerId, setWinnerId] = useState('');
    const [endReason, setEndReason] = useState('');
    const [phase, setPhase] = useState('DEFENSE');
    const [messages, setMessages] = useState([]);
    const [results, setResults] = useState(null);
    const [isTransitioning, setIsTransitioning] = useState(false);
    const [countdown, setCountdown] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    useEffect(() => {
        if (!socket) return;
        socket.emit('joinGameRoom', { gameId, userId });
        const loadGameState = async () => {
            setIsLoading(true);
            try {
                const data = await fetchGameState(gameId, userId);
                setMyMessageCount(data.myMessageCount);
                setOpponentMessageCount(data.opponentMessageCount);
                setPhase(data.game.phase);
                setGameComplete(data.isGameComplete);
                //console.log("CALLING LOAD GAME STATE");
                //console.log(data.transition.isTransitioning);
                if (data.isGameComplete) {
                    setWinnerId(data.winnerId);
                }
                if (data.transition.isTransitioning) {
                    setIsTransitioning(true);
                    setCountdown(data.transition.countdown);
                } else {
                    setIsTransitioning(false);
                    setCountdown(null);
                }
                if (data.gameTurns && data.gameTurns.length > 0) {
                    console.log(data.gameTurns);
                    const loadedMessages = data.gameTurns.flatMap(turn =>
                        [
                            { type: 'user', content: turn.playerMessage },
                            { type: 'ai', content: turn.aiResponse }
                        ]
                    );
                    console.log('loadedMessages structure:', loadedMessages);
                    console.log('First message:', loadedMessages[0]);
                    setMessages(loadedMessages);
                }
            } catch (error) {
                console.log('User not in game, spectating mode', error);
                setIsSpectating(true);
            }
            setIsLoading(false);
        };


        socket.on('turnSubmitted', (data) => {
            // data could be: { userId, messageCount }
            if (data.userId === userId) {
                setMyMessageCount(data.messageCount);
                setMessages(prev => [
                    ...prev, { type: 'user', content: data.playerMessage },
                    { type: 'ai', content: data.aiResponse }
                ]);
            } else {
                setOpponentMessageCount(data.messageCount);
            }
        });
        socket.on('gameComplete', (data) => {
            setGameComplete(true);
            setWinnerId(data.winnerId);
            //setResults(data.finalState);
        });
        socket.on('transitionPhase', (data) => {
            if (data.isTransitioning) {
                setIsTransitioning(true);
                setCountdown(data.countdown);
            } else {
                setIsTransitioning(false);
                setMessages([]);
                setMyMessageCount(0);
                setOpponentMessageCount(0);
                console.log(data);
            }

        })
        loadGameState();
        return () => {
            socket.off('turnSubmitted');
            socket.off('gameComplete');
            socket.off('transitionPhase');
        };

    }, [gameId, userId, socket]);
    const handleSubmitTurn = async (message) => {
        try {
            await SubmitTurn({ gameId, userId, message });
        } catch (error) {
            console.error('Error submitting turn:', error);
        }


    };
    const handleSubmit = () => {
        if (message.trim()) {
            handleSubmitTurn(message);
            setMessage('');
        }
    };

    return (
        <>
            {isTransitioning && (
                <TransitionCountdown countdown={countdown} nextPhase={'ATTACK'} />
            )}
            {isLoading ? (
                // Show loading spinner or blank screen while fetching
                <div className="w-full max-w-2xl mx-auto p-4 flex items-center justify-center min-h-screen">
                    <div className="text-[#DFD0B8] text-xl">Loading...</div>
                </div>
            ) : isSpectating ? (
                <SpectatingBanner />
            ) : (
                <div className="w-full max-w-2xl mx-auto p-4">
                    {/* Game Complete Banner */}
                    {gameComplete && <GameCompleteBanner winnerId={winnerId} />}

                    {/* Score Display */}
                    <ScoreDisplay
                        myMessageCount={myMessageCount}
                        opponentMessageCount={opponentMessageCount}
                    />

                    {/* Chat Messages */}
                    <ChatMessages messages={messages} />

                    {/* Turn Input Component */}
                    <TurnInput onSubmit={handleSubmitTurn} disabled={myMessageCount >= 2 || gameComplete} />
                </div>
            )}
        </>
    )
}