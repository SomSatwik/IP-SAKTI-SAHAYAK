import { useState, useRef, useEffect } from 'react';
import ForceGraph2D from 'react-force-graph-2d';
import { GitBranch, ExternalLink } from 'lucide-react';

const mockGraphData = {
  nodes: [
    { id: 'query', group: 1, label: 'Subject Matter: Turmeric', type: 'Query' },
    { id: 'd_tk', group: 2, label: 'Domain: Traditional Knowledge', type: 'Domain' },
    { id: 'd_patent', group: 2, label: 'Domain: Patent', type: 'Domain' },
    { id: 's_tkdl', group: 3, label: 'Source: TKDL', type: 'Source' },
    { id: 's_uspto', group: 3, label: 'Source: USPTO', type: 'Source' },
    { id: 'e_1', group: 4, label: 'Evidence: Ayurvedic texts mention wound healing', type: 'Evidence' },
    { id: 'e_2', group: 4, label: 'Evidence: US Patent 5,401,504', type: 'Evidence' },
    { id: 'r_1', group: 5, label: 'Risk: Patent Invalidity', type: 'Risk' }
  ],
  links: [
    { source: 'query', target: 'd_tk', value: 1 },
    { source: 'query', target: 'd_patent', value: 1 },
    { source: 'd_tk', target: 's_tkdl', value: 1 },
    { source: 'd_patent', target: 's_uspto', value: 1 },
    { source: 's_tkdl', target: 'e_1', value: 1 },
    { source: 's_uspto', target: 'e_2', value: 1 },
    { source: 'e_1', target: 'r_1', value: 2 },
    { source: 'e_2', target: 'r_1', value: 2 }
  ]
};

const colors = {
  Query: '#4f46e5', // indigo-600
  Domain: '#3b82f6', // blue-500
  Source: '#10b981', // emerald-500
  Evidence: '#64748b', // slate-500
  Risk: '#ef4444' // red-500
};

export default function EvidenceGraphPage() {
  const containerRef = useRef(null);
  const [dimensions, setDimensions] = useState({ width: 800, height: 600 });
  const [selectedNode, setSelectedNode] = useState(null);

  useEffect(() => {
    if (containerRef.current) {
      setDimensions({
        width: containerRef.current.clientWidth,
        height: containerRef.current.clientHeight
      });
    }
    
    const handleResize = () => {
      if (containerRef.current) {
        setDimensions({
          width: containerRef.current.clientWidth,
          height: containerRef.current.clientHeight
        });
      }
    };
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return (
    <div className="h-[calc(100vh-48px)] flex flex-col">
      <div className="mb-4 flex justify-between items-end shrink-0">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <GitBranch className="w-6 h-6 text-indigo-600" /> Evidence Graph
          </h1>
          <p className="text-sm text-slate-500 mt-1">Interactive visualization of knowledge extraction and entity relationships.</p>
        </div>
        <div className="flex gap-2">
          {Object.entries(colors).map(([label, color]) => (
            <div key={label} className="flex items-center gap-1.5 text-xs text-slate-600">
              <div className="w-3 h-3 rounded-full" style={{ backgroundColor: color }} />
              {label}
            </div>
          ))}
        </div>
      </div>

      <div className="flex-1 flex gap-4 overflow-hidden">
        <div 
          ref={containerRef}
          className="flex-1 bg-white border border-slate-200 rounded-lg shadow-sm overflow-hidden relative"
        >
          <ForceGraph2D
            width={dimensions.width}
            height={dimensions.height}
            graphData={mockGraphData}
            nodeLabel="label"
            nodeColor={node => colors[node.type] || '#cbd5e1'}
            nodeRelSize={6}
            linkColor={() => '#e2e8f0'}
            linkWidth={1.5}
            onNodeClick={setSelectedNode}
            d3Force="charge"
            d3ForceConfig={{ strength: -200 }}
          />
        </div>

        {selectedNode && (
          <div className="w-80 bg-white border border-slate-200 rounded-lg shadow-sm flex flex-col shrink-0 animate-in slide-in-from-right">
            <div className="p-4 border-b border-slate-100 flex justify-between items-start">
              <div>
                <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  {selectedNode.type}
                </span>
                <h3 className="font-semibold text-slate-900 mt-1">{selectedNode.label}</h3>
              </div>
              <button 
                onClick={() => setSelectedNode(null)}
                className="text-slate-400 hover:text-slate-600"
              >
                &times;
              </button>
            </div>
            <div className="p-4 flex-1 overflow-auto">
              <div className="space-y-4">
                <div>
                  <h4 className="text-xs font-medium text-slate-500 uppercase">Internal ID</h4>
                  <p className="text-sm text-slate-700 font-mono mt-1">{selectedNode.id}</p>
                </div>
                <div>
                  <h4 className="text-xs font-medium text-slate-500 uppercase">Connections</h4>
                  <p className="text-sm text-slate-700 mt-1">
                    {mockGraphData.links.filter(l => l.source.id === selectedNode.id || l.target.id === selectedNode.id).length} direct relationships
                  </p>
                </div>
                {selectedNode.type === 'Evidence' && (
                  <button className="w-full flex items-center justify-center gap-2 px-3 py-2 bg-indigo-50 text-indigo-700 rounded-md text-sm font-medium hover:bg-indigo-100 transition-colors">
                    View Source Document <ExternalLink className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
