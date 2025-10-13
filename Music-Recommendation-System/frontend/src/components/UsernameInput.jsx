export default function UsernameInput({ value, onChange, placeholder }) {
    return (
        <input
            type="text"
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className="bg-[#1f1f1f] text-white border rounded px-4 py-3 w-full text-xl
                       placeholder-gray-500 focus:border-white outline-none
                       focus:ring-2 focus:ring-white hover:ring-2  transition-all duration-200"
        />
    );
}
