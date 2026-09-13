import { useNavigate } from 'react-router-dom';
import { Search, Database, ShieldAlert, FileText, ArrowRight } from 'lucide-react';
import StatsCard from '../components/StatsCard';
import ConfidenceIndicator from '../components/ConfidenceIndicator';

export default function Dashboard() {
  const navigate = useNavigate();

  const handleDemoCase = async () => {
    navigate('/analysis'); // Simple routing for demo purposes
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Executive Dashboard</h1>
          <p className="text-sm text-slate-500 mt-1">System Overview and Recent Intelligence</p>
        </div>
        <div className="flex gap-3">
          <button 
            onClick={() => navigate('/analysis')}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md text-sm font-medium transition-colors shadow-sm"
          >
            Start New Investigation
          </button>
          <button 
            onClick={handleDemoCase}
            className="px-4 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 rounded-md text-sm font-medium transition-colors shadow-sm"
          >
            Load Demo Case
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard 
          title="Active Investigations" 
          value="12" 
          icon={Search} 
          trend={{ value: '+3', isPositive: true }} 
          description="from last week" 
        />
        <StatsCard 
          title="Sources Indexed" 
          value="45,231" 
          icon={Database} 
          description="across 9 IP domains" 
        />
        <StatsCard 
          title="Evidence Retrieved" 
          value="1,842" 
          icon={FileText} 
          trend={{ value: '+12%', isPositive: true }}
          description="this month" 
        />
        <StatsCard 
          title="High Risk Flags" 
          value="4" 
          icon={ShieldAlert} 
          trend={{ value: '-2', isPositive: true }}
          description="requiring attention" 
        />
      </div>

      <div className="mt-8">
        <h2 className="text-lg font-semibold text-slate-900 mb-4">Recent Investigations</h2>
        <div className="bg-white border border-slate-200 rounded-lg shadow-sm divide-y divide-slate-100">
          {[
            { id: 1, title: 'Turmeric Patent Prior Art Search', domains: ['Patent', 'Traditional Knowledge'], confidence: 0.92, date: '2 hours ago' },
            { id: 2, title: 'Kandhamal Haldi GI Infringement', domains: ['Geographical Indication'], confidence: 0.85, date: 'Yesterday' },
            { id: 3, title: 'Software UI Copyright Claim', domains: ['Copyright', 'Design'], confidence: 0.65, date: '3 days ago' },
          ].map((item) => (
            <div key={item.id} className="p-4 hover:bg-slate-50 transition-colors flex items-center justify-between group cursor-pointer" onClick={() => navigate('/analysis')}>
              <div>
                <h4 className="font-medium text-slate-900">{item.title}</h4>
                <div className="flex items-center gap-2 mt-1">
                  {item.domains.map(d => (
                    <span key={d} className="px-2 py-0.5 bg-slate-100 text-slate-600 text-xs rounded-full font-medium">
                      {d}
                    </span>
                  ))}
                  <span className="text-xs text-slate-400">• {item.date}</span>
                </div>
              </div>
              <div className="flex items-center gap-6">
                <ConfidenceIndicator score={item.confidence} />
                <ArrowRight className="w-5 h-5 text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
