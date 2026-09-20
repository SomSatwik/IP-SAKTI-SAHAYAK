"""
IP-SAKTI SAHAYAK
Knowledge Graph Service
=======================
Lightweight semantic network mapping concepts, statutory provisions,
authoritative bodies, and traditional formulations in Indian and International IP.
"""

from typing import Dict, Any, List


class KnowledgeGraphService:
    """Manages nodes and relational edges across IP and Traditional Knowledge domains."""

    def __init__(self):
        self._nodes: List[Dict[str, Any]] = [
            {"id": "ayurveda", "label": "Ayurveda", "group": "domain", "color": "#10b981"},
            {"id": "traditional_knowledge", "label": "Traditional Knowledge", "group": "domain", "color": "#059669"},
            {"id": "tkdl", "label": "TKDL (CSIR/Ayush)", "group": "repository", "color": "#3b82f6"},
            {"id": "patents_act", "label": "Patents Act, 1970", "group": "statute", "color": "#f59e0b"},
            {"id": "section_3d", "label": "Section 3(d) (Efficacy)", "group": "provision", "color": "#ef4444"},
            {"id": "section_3p", "label": "Section 3(p) (TK Bar)", "group": "provision", "color": "#ef4444"},
            {"id": "section_3e", "label": "Section 3(e) (Admixture)", "group": "provision", "color": "#ef4444"},
            {"id": "biodiversity_act", "label": "Biological Diversity Act, 2002", "group": "statute", "color": "#14b8a6"},
            {"id": "section_6", "label": "Section 6 (IP Clearance)", "group": "provision", "color": "#f97316"},
            {"id": "nba", "label": "National Biodiversity Authority", "group": "authority", "color": "#6366f1"},
            {"id": "form_iii", "label": "Form III (NBA Approval)", "group": "procedure", "color": "#8b5cf6"},
            {"id": "cgpdtm", "label": "Indian Patent Office (CGPDTM)", "group": "authority", "color": "#6366f1"},
            {"id": "pct", "label": "Patent Cooperation Treaty (PCT)", "group": "treaty", "color": "#0ea5e9"},
            {"id": "wipo", "label": "WIPO", "group": "authority", "color": "#0284c7"},
            {"id": "trips", "label": "WTO TRIPS (Art. 27)", "group": "treaty", "color": "#0284c7"},
            {"id": "ashwagandha", "label": "Withania somnifera (Ashwagandha)", "group": "botanical", "color": "#84cc16"},
            {"id": "turmeric", "label": "Curcuma longa (Turmeric)", "group": "botanical", "color": "#eab308"},
            {"id": "gi_act", "label": "Geographical Indications Act", "group": "statute", "color": "#d97706"}
        ]

        self._edges: List[Dict[str, Any]] = [
            {"from": "ayurveda", "to": "traditional_knowledge", "label": "encompassed by"},
            {"from": "traditional_knowledge", "to": "tkdl", "label": "digitized in"},
            {"from": "tkdl", "to": "cgpdtm", "label": "cited as prior art by"},
            {"from": "tkdl", "to": "wipo", "label": "shared under agreement with"},
            {"from": "traditional_knowledge", "to": "section_3p", "label": "bars patenting under"},
            {"from": "patents_act", "to": "section_3p", "label": "contains"},
            {"from": "patents_act", "to": "section_3d", "label": "contains"},
            {"from": "patents_act", "to": "section_3e", "label": "contains"},
            {"from": "cgpdtm", "to": "patents_act", "label": "administers"},
            {"from": "biodiversity_act", "to": "section_6", "label": "mandates"},
            {"from": "section_6", "to": "nba", "label": "requires approval from"},
            {"from": "section_6", "to": "form_iii", "label": "submitted via"},
            {"from": "form_iii", "to": "cgpdtm", "label": "prerequisite for grant at"},
            {"from": "pct", "to": "wipo", "label": "administered by"},
            {"from": "pct", "to": "cgpdtm", "label": "national phase entry into"},
            {"from": "ashwagandha", "to": "ayurveda", "label": "classical Rasayana herb in"},
            {"from": "ashwagandha", "to": "section_3p", "label": "triggers examination under"},
            {"from": "turmeric", "to": "traditional_knowledge", "label": "landmark revocation case in"},
            {"from": "traditional_knowledge", "to": "gi_act", "label": "protected collectively under"}
        ]

    def get_graph_data(self) -> Dict[str, Any]:
        """Return nodes and edges for visualization."""
        return {
            "nodes": self._nodes,
            "edges": self._edges,
            "node_count": len(self._nodes),
            "edge_count": len(self._edges)
        }


# Global singleton instance
knowledge_graph = KnowledgeGraphService()
