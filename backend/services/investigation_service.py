from typing import Dict, List, Optional
from datetime import datetime
import uuid
from ..models import InvestigationSummary, InvestigationDetail
from ..demo_data import demo_investigation, DEMO_INVESTIGATION_ID

class InvestigationService:
    def __init__(self):
        self.investigations: Dict[str, InvestigationDetail] = {
            DEMO_INVESTIGATION_ID: demo_investigation
        }

    def add_investigation(self, detail: InvestigationDetail) -> str:
        self.investigations[detail.id] = detail
        return detail.id

    def get_investigation(self, inv_id: str) -> Optional[InvestigationDetail]:
        return self.investigations.get(inv_id)

    def get_all_summaries(self) -> List[InvestigationSummary]:
        return [
            InvestigationSummary(
                id=inv.id,
                query=inv.query,
                timestamp=inv.timestamp,
                domain=inv.domain,
                status=inv.status
            )
            for inv in sorted(self.investigations.values(), key=lambda x: x.timestamp, reverse=True)
        ]

investigation_service = InvestigationService()
