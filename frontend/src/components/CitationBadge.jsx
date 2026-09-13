export default function CitationBadge({ id, onClick }) {
  return (
    <button
      onClick={onClick}
      className="inline-flex items-center justify-center w-4 h-4 text-[10px] font-bold text-indigo-700 bg-indigo-100 rounded hover:bg-indigo-200 transition-colors mx-0.5 align-super cursor-pointer"
      title="View citation"
    >
      {id}
    </button>
  );
}
