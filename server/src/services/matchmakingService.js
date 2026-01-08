class matchmakingService {

    constructor() {
        this.queue = [];
    }
    addPlayer(playerId, socketId) {
        this.queue.push({
            playerId,
            socketId,
            joinedAt: Date.now()
        });
        return this.tryMatch();
    }

    tryMatch() {
        // Just need any 2 players, no elo, no region just yet
        if (this.queue.length >= 2) {
            const [playerOne, playerTwo] = this.queue.splice(0, 2);
            return { playerOne, playerTwo };
        }
        return null;
    }

    removePlayer(playerId) {
        this.queue = this.queue.filter(p => p.playerId !== playerId);
    }
};
export default new matchmakingService();