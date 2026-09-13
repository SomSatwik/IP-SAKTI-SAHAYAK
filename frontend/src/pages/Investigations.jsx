import { Clock, Tag } from 'lucide-react';
import ConfidenceIndicator from '../components/ConfidenceIndicator';

const INV = [
  { id: 'INV-2026-001', title: 'Turmeric Patent Prior Art Search', domains: ['Patent', 'Traditional Knowledge'], confidence: 0.92, date: 'Oct 12, 2026' },
  { id: 'INV-2026-002', title: 'Kandhamal Haldi GI Infringement', domains: ['Geographical Indication'], confidence: 0.85, date: 'Oct 10, 2026' },
  { id: 'INV-2026-003', title: 'Software UI Copyright Claim', domains: ['Copyright', 'Design'], confidence: 0.65, date: 'Sep 28, 2026' },
];

export default function Investigations() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Saved Investigations</h1>
        <p className="text-sm text-slate-500 mt-1">History of your AI analyses and reports.</p>
      </div>

      <div className="grid gap-4">
        {INV.map(item => (
          <div key={item.id} className="bg-white border border-slate-200 rounded-lg p-5 shadow-sm hover:shadow-md transition-shadow cursor-pointer flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-mono text-slate-400">{item.id}</span>
                <span className="flex items-center gap-1 text-xs text-slate-500">
                  <Clock className="w-3 h-3" /> {item.date}
                </span>
              </div>
              <h3 className="text-lg font-semibold text-slate-900">{item.title}</h3>
              <div className="flex items-center gap-2 mt-3">
                <Tag className="w-4 h-4 text-slate-400" />
                {item.domains.map(d => (
                  <span key={d} className="px-2 py-1 bg-slate-100 text-slate-600 text-xs rounded-full font-medium">
                    {d}
                  </span>
                ))}
              </div>
            </div>
            <div className="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center gap-2">
              <ConfidenceIndicator score={item.confidence} size="sm" />
              <button className="text-indigo-600 text-sm font-medium hover:text-indigo-800">
                View Report &rarr;
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
