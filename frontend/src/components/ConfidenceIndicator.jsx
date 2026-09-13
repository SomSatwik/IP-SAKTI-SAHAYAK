import { Info } from 'lucide-react';

export default function ConfidenceIndicator({ score, size = 'md' }) {
  const percentage = Math.round(score * 100);
  
  let colorClass = 'text-slate-600 bg-slate-100';
  let barColor = 'bg-slate-400';
  
  if (percentage >= 80) {
    colorClass = 'text-emerald-700 bg-emerald-50';
    barColor = 'bg-emerald-500';
  } else if (percentage >= 50) {
    colorClass = 'text-amber-700 bg-amber-50';
    barColor = 'bg-amber-500';
  } else {
    colorClass = 'text-red-700 bg-red-50';
    barColor = 'bg-red-500';
  }

  const isSmall = size === 'sm';

  return (
    <div className={`flex items-center gap-2 ${isSmall ? 'text-xs' : 'text-sm'}`}>
      <span className="text-slate-500 font-medium">Confidence</span>
      <div className={`px-2 py-0.5 rounded flex items-center gap-1 font-semibold ${colorClass}`}>
        {percentage}%
      </div>
      {!isSmall && (
        <div className="w-24 h-1.5 bg-slate-200 rounded-full overflow-hidden">
          <div className={`h-full ${barColor}`} style={{ width: `${percentage}%` }} />
        </div>
      )}
    </div>
  );
}
