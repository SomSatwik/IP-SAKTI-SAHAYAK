import { NavLink } from 'react-router-dom';
import { LayoutDashboard, MessageSquare, Search, GitBranch, ClipboardList, BookOpen, FileText, FolderOpen, Settings } from 'lucide-react';

const NAV_ITEMS = [
  { path: '/', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/ask', label: 'Ask IP-SAKTI', icon: MessageSquare },
  { path: '/analysis', label: 'IP Analysis', icon: Search },
  { path: '/graph', label: 'Evidence Graph', icon: GitBranch },
  { path: '/roadmap', label: 'Compliance Roadmap', icon: ClipboardList },
  { path: '/knowledge', label: 'Knowledge Base', icon: BookOpen },
  { path: '/documents', label: 'Documents', icon: FileText },
  { path: '/investigations', label: 'Investigations', icon: FolderOpen },
  { path: '/settings', label: 'Settings', icon: Settings },
];

export default function Sidebar() {
  return (
    <aside className="w-64 bg-slate-900 text-white flex flex-col h-full border-r border-slate-800 shrink-0">
      <div className="p-6 border-b border-slate-800">
        <h1 className="text-xl font-bold tracking-tight text-white">IP-SAKTI SAHAYAK</h1>
        <p className="text-xs text-indigo-300 mt-1 uppercase tracking-wider font-semibold">AI-Powered IP Intelligence</p>
      </div>
      
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-md transition-colors text-sm font-medium ${
                isActive 
                  ? 'bg-indigo-600 text-white' 
                  : 'text-slate-300 hover:bg-slate-800 hover:text-white'
              }`
            }
          >
            <item.icon className="w-5 h-5" />
            {item.label}
          </NavLink>
        ))}
      </nav>
      
      <div className="p-4 border-t border-slate-800">
        <div className="bg-slate-800 rounded-md py-2 px-3 flex items-center justify-center">
          <span className="text-xs font-bold text-slate-300 tracking-widest">SIH 2026</span>
        </div>
      </div>
    </aside>
  );
}
