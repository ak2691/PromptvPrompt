export default function TransitionCountdown({ countdown, nextPhase }) {
    return (
        <div className="fixed inset-0 bg-[#222831] bg-opacity-90 flex items-center justify-center z-50">
            <div className="bg-[#393E46] rounded-lg px-12 py-8 text-center shadow-2xl border-2 border-[#948979]">
                <h2 className="text-[#DFD0B8] text-3xl font-bold mb-4">
                    Starting Phase {nextPhase}...
                </h2>
                <div className="text-[#948979] text-6xl font-bold">
                    {countdown}
                </div>
            </div>
        </div>
    );
}