import { Check } from 'lucide-react';

export default function Settings() {
  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">System Settings</h1>
        <p className="text-sm text-slate-500 mt-1">Configure your IP-SAKTI workspace.</p>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg shadow-sm overflow-hidden">
        <div className="p-6 border-b border-slate-200">
          <h2 className="text-lg font-semibold text-slate-900">Localization</h2>
          <p className="text-sm text-slate-500">Language and region preferences.</p>
          
          <div className="mt-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Interface Language</label>
              <select className="w-full max-w-xs p-2 border border-slate-200 rounded-md outline-none focus:border-indigo-500">
                <option>English</option>
                <option>Hindi (Preview)</option>
                <option disabled>Odia (Coming Soon)</option>
              </select>
            </div>
            
            <div className="flex items-center gap-3">
              <input type="checkbox" id="voice" disabled className="rounded border-slate-300 text-indigo-600" />
              <label htmlFor="voice" className="text-sm text-slate-500 line-through">Enable Voice Input (Microphone required)</label>
            </div>
          </div>
        </div>

        <div className="p-6">
          <h2 className="text-lg font-semibold text-slate-900">System Status</h2>
          <div className="mt-4 space-y-3">
            <div className="flex items-center justify-between p-3 bg-slate-50 rounded-md border border-slate-100">
              <span className="text-sm font-medium text-slate-700">API Connection</span>
              <span className="flex items-center gap-1.5 text-xs font-semibold text-emerald-600 bg-emerald-100 px-2 py-1 rounded-full">
                <Check className="w-3.5 h-3.5" /> Connected
              </span>
            </div>
            <div className="flex items-center justify-between p-3 bg-slate-50 rounded-md border border-slate-100">
              <span className="text-sm font-medium text-slate-700">Vector Database</span>
              <span className="flex items-center gap-1.5 text-xs font-semibold text-emerald-600 bg-emerald-100 px-2 py-1 rounded-full">
                <Check className="w-3.5 h-3.5" /> Synced
              </span>
            </div>
            <div className="flex items-center justify-between p-3 bg-slate-50 rounded-md border border-slate-100">
              <span className="text-sm font-medium text-slate-700">Version</span>
              <span className="text-sm text-slate-500 font-mono">v1.0.0-beta</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
