export default function SpectatingBanner() {
    return (
        <div className="w-full max-w-2xl mx-auto p-4">
            <div className="bg-[#393E46] rounded-lg px-6 py-8 text-center">
                <h2 className="text-[#DFD0B8] text-2xl font-bold mb-2">
                    You are spectating
                </h2>
                <p className="text-[#948979]">
                    You are not a player in this game
                </p>
            </div>
        </div>
    );
}