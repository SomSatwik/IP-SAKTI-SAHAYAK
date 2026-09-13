import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FileBadge, FileSignature, Copyright, Brush, Globe2, 
  BookMarked, Leaf, Network, Landmark 
} from 'lucide-react';
import LoadingSpinner from '../components/LoadingSpinner';

const DOMAINS = [
  { id: 'patent', label: 'Patent', icon: FileBadge, desc: 'Prior art and novelty' },
  { id: 'trademark', label: 'Trademark', icon: FileSignature, desc: 'Brand protection' },
  { id: 'copyright', label: 'Copyright', icon: Copyright, desc: 'Original works' },
  { id: 'design', label: 'Design', icon: Brush, desc: 'Industrial designs' },
  { id: 'gi', label: 'Geographical Indication', icon: Globe2, desc: 'Origin-based protection' },
  { id: 'tk', label: 'Traditional Knowledge', icon: BookMarked, desc: 'TKDL integration' },
  { id: 'ayurveda', label: 'Ayurveda', icon: Leaf, desc: 'AYUSH formulations' },
  { id: 'biodiversity', label: 'Biodiversity/ABS', icon: Network, desc: 'Access & Benefit Sharing' },
  { id: 'international', label: 'International IP', icon: Landmark, desc: 'WIPO & treaties' },
];

export default function IPAnalysis() {
  const navigate = useNavigate();
  const [selectedDomains, setSelectedDomains] = useState([]);
  const [caseDesc, setCaseDesc] = useState('');
  const [loading, setLoading] = useState(false);

  const toggleDomain = (id) => {
    setSelectedDomains(prev => 
      prev.includes(id) ? prev.filter(d => d !== id) : [...prev, id]
    );
  };

  const handleAnalyze = () => {
    if (!caseDesc.trim() || selectedDomains.length === 0) return;
    setLoading(true);
    setTimeout(() => {
      // Complete mock flow
      navigate('/graph');
    }, 2000);
  };

  if (loading) {
    return <LoadingSpinner message="Initiating deep cross-domain analysis..." />;
  }

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Start New IP Analysis</h1>
        <p className="text-sm text-slate-500 mt-1">Select domains and describe the subject matter for comprehensive investigation.</p>
      </div>

      <div className="space-y-4">
        <h3 className="text-sm font-semibold text-slate-900 uppercase tracking-wider">1. Select Target Domains</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {DOMAINS.map(domain => {
            const isSelected = selectedDomains.includes(domain.id);
            return (
              <div 
                key={domain.id}
                onClick={() => toggleDomain(domain.id)}
                className={`p-4 rounded-lg border cursor-pointer transition-all ${
                  isSelected 
                    ? 'border-indigo-600 bg-indigo-50 shadow-sm' 
                    : 'border-slate-200 bg-white hover:border-indigo-300 hover:bg-slate-50'
                }`}
              >
                <div className="flex items-center gap-3 mb-2">
                  <div className={`p-2 rounded-md ${isSelected ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600'}`}>
                    <domain.icon className="w-4 h-4" />
                  </div>
                  <h4 className={`font-semibold text-sm ${isSelected ? 'text-indigo-900' : 'text-slate-900'}`}>
                    {domain.label}
                  </h4>
                </div>
                <p className={`text-xs ${isSelected ? 'text-indigo-700' : 'text-slate-500'}`}>
                  {domain.desc}
                </p>
              </div>
            );
          })}
        </div>
      </div>

      <div className="space-y-4">
        <h3 className="text-sm font-semibold text-slate-900 uppercase tracking-wider">2. Case Description</h3>
        <textarea
          value={caseDesc}
          onChange={(e) => setCaseDesc(e.target.value)}
          placeholder="Describe the invention, brand, or traditional practice in detail..."
          className="w-full h-40 p-4 rounded-lg bg-white border border-slate-200 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none text-sm text-slate-700"
        />
      </div>

      <div className="flex justify-end pt-4">
        <button
          onClick={handleAnalyze}
          disabled={selectedDomains.length === 0 || !caseDesc.trim()}
          className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-md font-medium transition-colors shadow-sm"
        >
          Begin Analysis Workflow
        </button>
      </div>
    </div>
  );
}
