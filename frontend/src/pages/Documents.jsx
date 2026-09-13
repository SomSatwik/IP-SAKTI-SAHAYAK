import { UploadCloud, FileText, CheckCircle, Clock } from 'lucide-react';

const DOCS = [
  { id: 1, name: 'Client_Invention_Disclosure_Draft.pdf', status: 'ready', time: '10 mins ago', size: '2.4 MB' },
  { id: 2, name: 'Internal_Memo_Design.docx', status: 'processing', time: 'Just now', size: '840 KB' }
];

export default function Documents() {
  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Document Management</h1>
        <p className="text-sm text-slate-500 mt-1">Upload private cases and internal documents for secure vector embedding.</p>
      </div>

      <div className="border-2 border-dashed border-slate-300 rounded-xl bg-slate-50 p-12 text-center hover:bg-slate-100 transition-colors cursor-pointer">
        <UploadCloud className="w-12 h-12 text-slate-400 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-slate-900">Drag and drop documents</h3>
        <p className="text-slate-500 text-sm mt-1">PDF, DOCX, TXT up to 50MB</p>
        <button className="mt-6 px-4 py-2 bg-indigo-600 text-white rounded text-sm font-medium shadow-sm hover:bg-indigo-700">
          Browse Files
        </button>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg shadow-sm">
        <div className="px-6 py-4 border-b border-slate-200">
          <h3 className="font-semibold text-slate-900">Processing Queue & History</h3>
        </div>
        <div className="divide-y divide-slate-100">
          {DOCS.map(doc => (
            <div key={doc.id} className="p-4 px-6 flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div className="p-2 bg-slate-100 rounded text-slate-500">
                  <FileText className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-medium text-slate-900 text-sm">{doc.name}</p>
                  <p className="text-xs text-slate-500">{doc.size} • {doc.time}</p>
                </div>
              </div>
              <div>
                {doc.status === 'ready' ? (
                  <span className="flex items-center gap-1.5 px-2.5 py-1 bg-emerald-100 text-emerald-700 text-xs font-semibold rounded-full">
                    <CheckCircle className="w-3.5 h-3.5" /> Ready for Query
                  </span>
                ) : (
                  <span className="flex items-center gap-1.5 px-2.5 py-1 bg-indigo-50 text-indigo-700 text-xs font-semibold rounded-full">
                    <Clock className="w-3.5 h-3.5 animate-pulse" /> Processing...
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
