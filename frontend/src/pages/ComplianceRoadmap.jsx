import { CheckCircle2, Circle, AlertCircle } from 'lucide-react';
import RiskBadge from '../components/RiskBadge';

const STEPS = [
  {
    id: 1,
    title: 'Prior Art Validation',
    description: 'Verify extracted traditional knowledge references against WIPO standards.',
    status: 'completed',
    priority: 'HIGH',
    evidence: 'TKDL-VG/123, USPTO-5,401,504',
    action: 'Document generated.'
  },
  {
    id: 2,
    title: 'Draft Third-Party Observation',
    description: 'Prepare pre-grant opposition based on Section 3(p) of Indian Patent Act.',
    status: 'current',
    priority: 'HIGH',
    evidence: 'Indian Patent Act 1970',
    action: 'Review draft argument.'
  },
  {
    id: 3,
    title: 'Submit to Patent Office',
    description: 'File the observation with the relevant patent authority.',
    status: 'pending',
    priority: 'MEDIUM',
    evidence: null,
    action: 'Requires legal signature.'
  }
];

export default function ComplianceRoadmap() {
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Compliance Roadmap</h1>
        <p className="text-sm text-slate-500 mt-1">Actionable step-by-step guidance based on AI analysis.</p>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8">
        <div className="relative border-l-2 border-slate-100 ml-4 space-y-8">
          {STEPS.map((step, idx) => (
            <div key={step.id} className="relative pl-8">
              {/* Step circle indicator */}
              <div className="absolute -left-[17px] top-1 bg-white p-1">
                {step.status === 'completed' ? (
                  <CheckCircle2 className="w-6 h-6 text-emerald-500" />
                ) : step.status === 'current' ? (
                  <AlertCircle className="w-6 h-6 text-indigo-600" />
                ) : (
                  <Circle className="w-6 h-6 text-slate-300" />
                )}
              </div>

              <div className={`p-5 rounded-lg border ${step.status === 'current' ? 'border-indigo-200 bg-indigo-50 shadow-sm' : 'border-slate-100 bg-white'}`}>
                <div className="flex justify-between items-start mb-2">
                  <h3 className={`font-bold text-lg ${step.status === 'current' ? 'text-indigo-900' : 'text-slate-900'}`}>
                    {idx + 1}. {step.title}
                  </h3>
                  <RiskBadge level={step.priority} />
                </div>
                
                <p className="text-slate-600 text-sm mb-4">{step.description}</p>
                
                <div className="flex flex-col sm:flex-row sm:items-center gap-4 text-xs">
                  {step.evidence && (
                    <div className="flex items-center gap-2 text-slate-500">
                      <span className="font-semibold text-slate-700">Evidence:</span> 
                      {step.evidence}
                    </div>
                  )}
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-700">Action:</span>
                    <span className={step.status === 'current' ? 'text-indigo-600 font-medium' : 'text-slate-500'}>
                      {step.action}
                    </span>
                  </div>
                </div>

                {step.status === 'current' && (
                  <div className="mt-4 pt-4 border-t border-indigo-100 flex gap-3">
                    <button className="px-4 py-2 bg-indigo-600 text-white rounded text-sm font-medium hover:bg-indigo-700 transition">
                      Execute Action
                    </button>
                    <button className="px-4 py-2 bg-white text-indigo-600 border border-indigo-200 rounded text-sm font-medium hover:bg-indigo-50 transition">
                      Skip for now
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
