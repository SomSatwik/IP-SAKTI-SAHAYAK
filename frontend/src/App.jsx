import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import AskSakti from './pages/AskSakti';
import IPAnalysis from './pages/IPAnalysis';
import EvidenceGraphPage from './pages/EvidenceGraphPage';
import ComplianceRoadmap from './pages/ComplianceRoadmap';
import KnowledgeBase from './pages/KnowledgeBase';
import Documents from './pages/Documents';
import Investigations from './pages/Investigations';
import Settings from './pages/Settings';

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="ask" element={<AskSakti />} />
          <Route path="analysis" element={<IPAnalysis />} />
          <Route path="graph" element={<EvidenceGraphPage />} />
          <Route path="roadmap" element={<ComplianceRoadmap />} />
          <Route path="knowledge" element={<KnowledgeBase />} />
          <Route path="documents" element={<Documents />} />
          <Route path="investigations" element={<Investigations />} />
          <Route path="settings" element={<Settings />} />
        </Route>
      </Routes>
    </Router>
  );
}
