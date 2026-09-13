import { Search, Filter, Book, FileText, Download } from 'lucide-react';

const SOURCES = [
  { id: 1, title: 'Indian Patent Act, 1970', auth: 'Govt. of India', type: 'Statute', domain: 'Patent' },
  { id: 2, title: 'TKDL - Ayurveda Section', auth: 'CSIR', type: 'Database', domain: 'Traditional Knowledge' },
  { id: 3, title: 'Geographical Indications of Goods Act', auth: 'Govt. of India', type: 'Statute', domain: 'GI' },
  { id: 4, title: 'Madrid Protocol Guidelines', auth: 'WIPO', type: 'Treaty', domain: 'Trademark' },
];

export default function KnowledgeBase() {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Knowledge Base</h1>
          <p className="text-sm text-slate-500 mt-1">Authoritative sources indexed by IP-SAKTI.</p>
        </div>
        <button className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-md text-sm font-medium flex items-center gap-2 shadow-sm transition">
          <Download className="w-4 h-4" />
          Export Index
        </button>
      </div>

      <div className="bg-white p-4 border border-slate-200 rounded-lg shadow-sm flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
          <input 
            type="text" 
            placeholder="Search sources..." 
            className="w-full pl-9 pr-4 py-2 rounded border border-slate-200 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none"
          />
        </div>
        <div className="flex gap-2">
          <select className="py-2 px-3 border border-slate-200 rounded text-sm text-slate-600 outline-none bg-white">
            <option>All Authorities</option>
            <option>Govt. of India</option>
            <option>CSIR</option>
            <option>WIPO</option>
          </select>
          <select className="py-2 px-3 border border-slate-200 rounded text-sm text-slate-600 outline-none bg-white">
            <option>All Domains</option>
            <option>Patent</option>
            <option>TK</option>
            <option>GI</option>
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {SOURCES.map(source => (
          <div key={source.id} className="bg-white border border-slate-200 rounded-lg p-5 hover:shadow-md transition-shadow">
            <div className="flex gap-3 items-start mb-3">
              <div className="p-2 bg-indigo-50 text-indigo-600 rounded">
                {source.type === 'Database' ? <Book className="w-5 h-5" /> : <FileText className="w-5 h-5" />}
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 line-clamp-2">{source.title}</h3>
                <p className="text-xs text-slate-500 mt-1">{source.auth}</p>
              </div>
            </div>
            <div className="flex items-center gap-2 mt-4 pt-4 border-t border-slate-100">
              <span className="px-2 py-1 bg-slate-100 text-slate-600 text-xs rounded font-medium">
                {source.type}
              </span>
              <span className="px-2 py-1 bg-slate-100 text-slate-600 text-xs rounded font-medium">
                {source.domain}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
