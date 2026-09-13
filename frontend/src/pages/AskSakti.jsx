import { useState } from 'react';
import { Send, AlertTriangle } from 'lucide-react';
import LoadingSpinner from '../components/LoadingSpinner';
import EvidenceCard from '../components/EvidenceCard';
import ConfidenceIndicator from '../components/ConfidenceIndicator';
import CitationBadge from '../components/CitationBadge';

export default function AskSakti() {
  const [query, setQuery] = useState('');
  const [mode, setMode] = useState('quick');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;
    
    setLoading(true);
    // Simulate API call
    setTimeout(() => {
      setResult({
        answer: "Based on the provided IP databases and Traditional Knowledge Digital Library (TKDL), turmeric (Curcuma longa) has documented prior art for wound healing in Ayurvedic texts dating back centuries. The 1995 patent granted to University of Mississippi Medical Center was revoked by USPTO after CSIR presented these exact textual references.",
        confidence: 0.94,
        evidence: [
          {
            id: 1,
            documentName: "TKDL Reference Ayurveda: VG/123",
            authority: "CSIR / TKDL",
            content: "Turmeric paste application for wound healing is described extensively in ancient texts.",
            relevanceScore: 0.98,
            confidence: 0.99,
            date: "1997"
          },
          {
            id: 2,
            documentName: "USPTO Reexamination Certificate B1 5,401,504",
            authority: "USPTO",
            content: "The patent claims for the use of turmeric in wound healing are cancelled due to prior art.",
            relevanceScore: 0.95,
            confidence: 1.0,
            date: "1998"
          }
        ]
      });
      setLoading(false);
    }, 2000);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Ask IP-SAKTI</h1>
        <p className="text-sm text-slate-500 mt-1">Natural language querying over cross-domain IP intelligence.</p>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg shadow-sm overflow-hidden p-1">
        <div className="flex border-b border-slate-100 p-2 gap-2">
          <button 
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${mode === 'quick' ? 'bg-indigo-50 text-indigo-700' : 'text-slate-500 hover:bg-slate-50'}`}
            onClick={() => setMode('quick')}
          >
            Quick Answer
          </button>
          <button 
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${mode === 'deep' ? 'bg-indigo-50 text-indigo-700' : 'text-slate-500 hover:bg-slate-50'}`}
            onClick={() => setMode('deep')}
          >
            Deep Analysis
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="p-4 relative">
          <textarea
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="e.g. Is there any prior art for using turmeric to heal wounds?"
            className="w-full min-h-[120px] p-3 rounded-md bg-slate-50 border border-slate-200 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none resize-y text-slate-700"
          />
          <div className="flex justify-end mt-3">
            <button 
              type="submit"
              disabled={loading || !query.trim()}
              className="flex items-center gap-2 px-5 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-md text-sm font-medium transition-colors shadow-sm"
            >
              <Send className="w-4 h-4" />
              Analyze
            </button>
          </div>
        </form>
      </div>

      {loading && (
        <LoadingSpinner 
          message="Analyzing query..." 
          steps={[
            { label: 'Deconstructing intent & extracting entities', status: 'done' },
            { label: 'Retrieving evidence from vector store', status: 'current' },
            { label: 'Verifying sources against authorities', status: 'pending' },
            { label: 'Generating verifiable response', status: 'pending' }
          ]} 
        />
      )}

      {result && !loading && (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
          {result.confidence < 0.5 ? (
            <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 flex items-start gap-3">
              <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
              <div>
                <h4 className="font-semibold text-amber-900">Insufficient Evidence</h4>
                <p className="text-sm text-amber-800 mt-1">The system could not find enough authoritative sources to provide a high-confidence answer. Please refine your query.</p>
              </div>
            </div>
          ) : (
            <>
              <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-6">
                <div className="flex justify-between items-start mb-4">
                  <h3 className="text-lg font-semibold text-slate-900">Analysis Result</h3>
                  <ConfidenceIndicator score={result.confidence} />
                </div>
                
                <div className="prose prose-slate max-w-none text-slate-700 leading-relaxed">
                  <p>
                    Based on the provided IP databases and Traditional Knowledge Digital Library (TKDL), turmeric (Curcuma longa) has documented prior art for wound healing in Ayurvedic texts dating back centuries <CitationBadge id="1" />. The 1995 patent granted to University of Mississippi Medical Center was revoked by USPTO after CSIR presented these exact textual references <CitationBadge id="2" />.
                  </p>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider mb-3 ml-1">Retrieved Evidence</h3>
                <div className="grid gap-4">
                  {result.evidence.map(ev => (
                    <EvidenceCard key={ev.id} evidence={ev} />
                  ))}
                </div>
              </div>
            </>
          )}
          
          <div className="text-center text-xs text-slate-400 mt-8 pt-4 border-t border-slate-200">
            DISCLAIMER: This system provides AI-assisted intelligence. All results must be verified by legal counsel before taking action.
          </div>
        </div>
      )}
    </div>
  );
}
