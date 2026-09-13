import { useState } from 'react';
import { ExternalLink, ChevronDown, ChevronUp } from 'lucide-react';
import ConfidenceIndicator from './ConfidenceIndicator';

export default function EvidenceCard({ evidence }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div 
      className={`border border-slate-200 bg-white rounded-lg shadow-sm overflow-hidden transition-all duration-200 ${expanded ? 'col-span-full' : ''}`}
    >
      <div 
        className="p-4 cursor-pointer hover:bg-slate-50 flex items-start justify-between"
        onClick={() => setExpanded(!expanded)}
      >
        <div className="flex-1 pr-4">
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
              {evidence.authority || 'Source'}
            </span>
            <span className="text-xs text-slate-500">
              Relevance: {(evidence.relevanceScore * 100).toFixed(0)}%
            </span>
          </div>
          <h4 className="font-medium text-slate-900 text-sm">{evidence.documentName}</h4>
          <p className="text-xs text-slate-500 mt-1 truncate">
            {evidence.section ? `Section: ${evidence.section}` : ''} 
            {evidence.page ? ` • Page: ${evidence.page}` : ''}
          </p>
        </div>
        
        <div className="flex items-center gap-3 shrink-0">
          <ConfidenceIndicator score={evidence.confidence} size="sm" />
          {expanded ? <ChevronUp className="w-5 h-5 text-slate-400" /> : <ChevronDown className="w-5 h-5 text-slate-400" />}
        </div>
      </div>

      {expanded && (
        <div className="p-4 border-t border-slate-100 bg-slate-50">
          <div className="prose prose-sm prose-slate max-w-none text-slate-700">
            <p className="whitespace-pre-wrap">{evidence.content}</p>
          </div>
          
          <div className="mt-4 flex items-center justify-between pt-4 border-t border-slate-200">
            <div className="text-xs text-slate-500 flex gap-4">
              <span>Date: {evidence.date || 'Unknown'}</span>
              <span>Type: {evidence.type || 'Document'}</span>
            </div>
            {evidence.url && (
              <a 
                href={evidence.url}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-800 font-medium"
              >
                View Original <ExternalLink className="w-4 h-4" />
              </a>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
