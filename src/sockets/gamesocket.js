
import matchmakingService from "../services/matchmakingService";
export default function setupGameSocket(io) {
    io.on('connection', (socket) => {
        socket.on('joinQueue', async ({ userId }) => {
            const match = matchmakingService.addPlayer(userId, socket.id);

            if (match) {
                const game = await gameService.createGameFromMatch(
                    match.playerOne.playerId,
                    match.playerTwo.playerId,
                );

                io.to(match.playerOne.socketId).emit('gameFound', { game });
                io.to(match.playerTwo.socketId).emit('gameFound', { game });
            } else {
                socket.emit('queueJoined', { position: matchmakingService.queue.length });
            }
        });
    })
}

