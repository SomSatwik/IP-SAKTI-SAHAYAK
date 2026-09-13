import { Loader2 } from 'lucide-react';

export default function LoadingSpinner({ message = 'Processing...', steps = [] }) {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-slate-500">
      <Loader2 className="w-8 h-8 animate-spin text-indigo-600 mb-4" />
      <p className="font-medium text-slate-700">{message}</p>
      
      {steps.length > 0 && (
        <div className="mt-6 w-full max-w-sm space-y-3">
          {steps.map((step, idx) => (
            <div key={idx} className="flex items-center gap-3 text-sm">
              <div className={`w-2 h-2 rounded-full ${step.status === 'done' ? 'bg-emerald-500' : step.status === 'current' ? 'bg-indigo-500 animate-pulse' : 'bg-slate-200'}`} />
              <span className={step.status === 'done' ? 'text-slate-400' : step.status === 'current' ? 'text-indigo-700 font-medium' : 'text-slate-400'}>
                {step.label}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
