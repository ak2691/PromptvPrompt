export default function ChatMessages({ messages }) {
    return (
        <div className="bg-[#393E46] rounded-lg p-4 mb-4 max-h-96 overflow-y-auto">
            {messages.length === 0 ? (
                <p className="text-[#948979] text-center py-8">
                    Enter your prompts
                </p>
            ) : (
                <div className="space-y-4">
                    {messages.map((message, index) => (
                        <div
                            key={index}
                            className={`flex ${message.type === 'user' ? 'justify-start' : 'justify-end'}`}
                        >
                            <div
                                className={`max-w-[80%] rounded-lg px-4 py-2 ${message.type === 'user'
                                        ? 'bg-[#948979] text-[#222831]'
                                        : 'bg-[#DFD0B8] text-[#222831]'
                                    }`}
                            >
                                <p className="text-sm font-medium mb-1">
                                    {message.type === 'user' ? 'You' : 'AI'}
                                </p>
                                <p className="whitespace-pre-wrap">{message.content}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}