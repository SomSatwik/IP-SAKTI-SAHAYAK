"""
IP-SAKTI SAHAYAK
Dynamic Evidence Graph Service
===============================
Constructs a unified, multi-domain relational graph connecting:
User Query → Product → Ingredients → Claims → IP Findings →
Regulatory Requirements → Authorities → Sources & Evidence.
"""

from typing import List, Dict, Any, Optional
from ..models import (
    EvidenceGraphResponse, GraphNode, GraphEdge,
    ProductDetails, RegulatoryAnalysisResult
)

class GraphService:
    def build_unified_graph(
        self,
        query: str,
        product: ProductDetails,
        ip_findings: List[str],
        regulatory_result: Optional[RegulatoryAnalysisResult],
        jurisdictions: List[str]
    ) -> EvidenceGraphResponse:
        nodes: List[GraphNode] = []
        edges: List[GraphEdge] = []
        node_ids = set()

        def add_node(node_id: str, label: str, node_type: str, group: str = None, color: str = None):
            if node_id not in node_ids:
                node_ids.add(node_id)
                nodes.append(GraphNode(
                    id=node_id,
                    label=label,
                    type=node_type,
                    group=group or node_type.lower(),
                    color=color
                ))

        def add_edge(src: str, tgt: str, relation: str):
            edges.append(GraphEdge(source=src, target=tgt, relation=relation))

        # 1. Query Node
        add_node("user_query", "User Investigation Query", "Query", color="#3b82f6")

        # 2. Product Node
        p_name = product.name or "Ayurvedic Formulation"
        add_node("product_node", p_name, "Product", color="#10b981")
        add_edge("user_query", "product_node", "analyzes")

        # 3. Ingredients
        for herb in (product.ingredients or ["Ashwagandha", "Curcumin"]):
            herb_id = f"herb_{herb.lower().replace(' ', '_')}"
            add_node(herb_id, herb, "Botanical", color="#84cc16")
            add_edge("product_node", herb_id, "contains_botanical")

            # Link herb to Traditional Knowledge / TKDL
            add_node("tkdl_repo", "TKDL Repository (CSIR)", "Repository", color="#0284c7")
            add_edge(herb_id, "tkdl_repo", "documented_in")

        # 4. IP Barriers & Patentability
        add_node("patents_act", "Indian Patents Act, 1970", "Statute", color="#f59e0b")
        add_node("sec_3p", "Section 3(p) TK Exclusion", "IP_Risk", color="#ef4444")
        add_node("sec_3e", "Section 3(e) Synergy Requirement", "IP_Requirement", color="#f97316")
        add_node("cgpdtm", "Patent Office (CGPDTM)", "Authority", color="#6366f1")

        add_edge("product_node", "sec_3p", "evaluated_under")
        add_edge("sec_3p", "patents_act", "statutory_ground_in")
        add_edge("sec_3p", "cgpdtm", "examined_by")
        add_edge("sec_3p", "sec_3e", "overcome_by_proving")

        # 5. Regulatory Requirements & Classification
        if regulatory_result:
            cat_name = regulatory_result.classification.potentialCategory
            add_node("reg_category", cat_name, "Classification", color="#059669")
            add_edge("product_node", "reg_category", "classified_as")

            add_node("dc_act", "Drugs & Cosmetics Act, 1940", "Statute", color="#d97706")
            add_node("rule_158b", "Rule 158B (Manufacturing License)", "Requirement", color="#10b981")
            add_node("sla_authority", "State Ayush Licensing Authority", "Authority", color="#6366f1")

            add_edge("reg_category", "rule_158b", "governed_by")
            add_edge("rule_158b", "dc_act", "rule_under")
            add_edge("rule_158b", "sla_authority", "administered_by")

            # ABS / Biodiversity
            add_node("nba_authority", "National Biodiversity Authority (NBA)", "Authority", color="#6366f1")
            add_node("form_iii", "Form III (IP Approval)", "Procedure", color="#8b5cf6")
            add_edge("product_node", "form_iii", "mandates")
            add_edge("form_iii", "nba_authority", "submitted_to")
            add_edge("form_iii", "cgpdtm", "prerequisite_for_grant_at")

        # 6. International Nodes (if USA / EU present)
        if any(j.upper() in ["USA", "US", "UNITED STATES"] for j in jurisdictions):
            add_node("us_jurisdiction", "United States (US FDA)", "Jurisdiction", color="#0ea5e9")
            add_node("dshea_act", "DSHEA 1994 (Dietary Supplement)", "Statute", color="#f59e0b")
            add_node("fda_authority", "US Food & Drug Administration (FDA)", "Authority", color="#6366f1")

            add_edge("product_node", "us_jurisdiction", "target_market")
            add_edge("us_jurisdiction", "dshea_act", "regulated_under")
            add_edge("dshea_act", "fda_authority", "enforced_by")

        return EvidenceGraphResponse(nodes=nodes, edges=edges)

graph_service = GraphService()
