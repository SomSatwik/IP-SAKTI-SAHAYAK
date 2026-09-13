import { AlertTriangle, ShieldCheck, ShieldAlert } from 'lucide-react';

export default function RiskBadge({ level }) {
  let config = {
    bg: 'bg-slate-100',
    text: 'text-slate-700',
    icon: ShieldCheck,
    label: 'Unknown'
  };

  if (level === 'LOW') {
    config = { bg: 'bg-emerald-100', text: 'text-emerald-700', icon: ShieldCheck, label: 'Low Risk' };
  } else if (level === 'MEDIUM') {
    config = { bg: 'bg-amber-100', text: 'text-amber-700', icon: AlertTriangle, label: 'Medium Risk' };
  } else if (level === 'HIGH') {
    config = { bg: 'bg-red-100', text: 'text-red-700', icon: ShieldAlert, label: 'High Risk' };
  }

  const Icon = config.icon;

  return (
    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${config.bg} ${config.text}`}>
      <Icon className="w-3.5 h-3.5" />
      {config.label}
    </div>
  );
}
