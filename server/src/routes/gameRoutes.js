import express from 'express';
const router = express.Router();
import GameService from '../services/gameService.js';
router.post('/game/:gameId/submit-turn', async (req, res) => {
    const { gameId } = req.params;
    const { userId, message } = req.body;
    //need a check to see if userId is part of game
    try {
        // Gather information about game state to emit to sockets
        const game = await GameService.getGame(gameId);
        if (!game) {
            return res.status(404).json({ error: 'Game not found' });
        }
        if (game.playerOneId !== userId && game.playerTwoId !== userId) {
            throw new Error("You are spectating");
        }
        const phaseBefore = await GameService.getPhase(gameId);
        const result = await GameService.submitTurn(gameId, userId, message);
        const phaseAfter = await GameService.getPhase(gameId);
        const isGameComplete = await GameService.checkGameEnd(gameId);
        const isTransition = phaseBefore !== phaseAfter ? true : false;

        const io = req.app.get('io');


        //Every turn checks the state of the game
        io.to(`game-${gameId}`).emit('turnSubmitted', {
            userId: userId,
            messageCount: result.turnNumber,
            playerMessage: result.playerMessage,
            aiResponse: result.aiResponse,
            isTransition
        });
        const updatedGameData = await GameService.getGame(gameId);
        if (isTransition) {


            let countdown = 5;
            const countdownInterval = setInterval(async () => {
                io.to(`game-${gameId}`).emit('transitionPhase', {
                    defenseSummary: userId === updatedGameData.playerOneId ? updatedGameData.playerTwoDefenseSummary : updatedGameData.playerOneDefenseSummary,
                    isTransitioning: true,
                    countdown: countdown

                });
                countdown--;
                if (countdown < 0) {
                    clearInterval(countdownInterval);
                    await GameService.endTransition(gameId);

                    io.to(`game-${gameId}`).emit('transitionPhase', {
                        isTransitioning: false
                    });
                }
            }, 1000);

        }
        if (isGameComplete) {

            io.to(`game-${gameId}`).emit('gameComplete', {
                winnerId: updatedGameData.winnerId,
                endReason: updatedGameData.endReason
            });
        }

        res.json({ result, isGameComplete, isTransition });
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
});

router.get('/game/:gameId', async (req, res) => {

    const { gameId } = req.params;
    const { userId } = req.query;
    let transitionData = { isTransitioning: false };
    const io = req.app.get('io');
    try {
        const game = await GameService.getGame(gameId);

        if (!game) {
            return res.status(404).json({ error: 'Game not found' });
        }
        if (game.playerOneId !== userId && game.playerTwoId !== userId) {
            throw new Error("You are spectating");
        }
        if (game.isTransitioning && game.transitionEndsAt) {
            const remainingMs = game.transitionEndsAt - Date.now();
            const remainingSeconds = Math.ceil(remainingMs / 1000);

            if (remainingSeconds > 0) {
                transitionData = {
                    isTransitioning: true,
                    countdown: remainingSeconds,
                    newPhase: game.currentPhase
                };
            } else {

                game.isTransitioning = false;
                game.transitionEndsAt = null;
            }
        }

        const myMessageCount = await GameService.getTurnCount(gameId, userId, game.phase);

        const opponentId = game.playerOneId === userId ? game.playerTwoId : game.playerOneId;
        const opponentMessageCount = await GameService.getTurnCount(gameId, opponentId, game.phase);
        const gameTurns = await GameService.getTurns(gameId, userId, game.phase);
        const isGameComplete = await GameService.checkGameEnd(gameId);




        res.json({
            myMessageCount,
            opponentMessageCount,
            game,
            gameTurns,
            isGameComplete,
            transition: transitionData

        });
    } catch (error) {
        res.status(400).json({ error: error.message });
    }

});

export default router;