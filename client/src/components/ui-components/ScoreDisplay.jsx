export default function ScoreDisplay({ myMessageCount, opponentMessageCount }) {
    return (
        <div className="flex gap-4 mb-4">
            <div className="bg-[#393E46] rounded-lg px-4 py-2 flex-1">
                <p className="text-[#DFD0B8] font-medium">
                    Your prompts: <span className="text-[#948979]">{myMessageCount}/5</span>
                </p>
            </div>
            <div className="bg-[#393E46] rounded-lg px-4 py-2 flex-1">
                <p className="text-[#DFD0B8] font-medium">
                    Opponent: <span className="text-[#948979]">{opponentMessageCount}/5</span>
                </p>
            </div>
        </div>
    );
}