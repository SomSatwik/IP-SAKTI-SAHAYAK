"""
IP-SAKTI SAHAYAK
Regulatory Change Monitor
=========================
Tracks gazette notifications, circulars, and rule amendments from IP India,
Ministry of Ayush, and National Biodiversity Authority (NBA).
"""

from typing import List, Dict, Any


class RegulationMonitor:
    """Monitors and retrieves recent official IP/Ayurveda regulatory updates."""

    SEED_UPDATES: List[Dict[str, Any]] = [
        {
            "id": "reg_001",
            "source_name": "National Biodiversity Authority (NBA)",
            "title": "Streamlined 90-Day Online Form III Processing for Startups",
            "notification_number": "NBA/ABS/2026/04",
            "category": "Biodiversity & ABS",
            "summary": "Expedited clearance window for Indian start-ups filing patent applications based on biological resources and cultivated medicinal plants.",
            "source_url": "https://nbaindia.org/circulars",
            "issued_date": "2026-01-15",
            "status": "Active Policy"
        },
        {
            "source_name": "Ministry of Ayush",
            "title": "Guidelines on Pharmacopoeial Standards for ASU Drugs (2025 Revision)",
            "notification_number": "AYUSH-NOTIF-2025/11",
            "category": "Ayurveda & Pharmacopoeia",
            "summary": "Updated testing parameters for heavy metals, microbial limits, and standardized marker compounds in classical formulations.",
            "source_url": "https://ayush.gov.in/notifications",
            "issued_date": "2025-11-20",
            "status": "In Force"
        },
        {
            "source_name": "Indian Patent Office (CGPDTM)",
            "title": "Updated Guidelines on Traditional Knowledge & Section 3(p) Objections",
            "notification_number": "CGPDTM/TK/2025/09",
            "category": "Patents & TKDL",
            "summary": "Mandatory cross-examination of TKDL classifications during First Examination Reports (FER) for natural formulations.",
            "source_url": "https://ipindia.gov.in/public-notices.htm",
            "issued_date": "2025-09-30",
            "status": "Statutory Guidance"
        },
        {
            "source_name": "WIPO / IGC Treaty",
            "title": "Treaty on Intellectual Property, Genetic Resources and Associated Traditional Knowledge",
            "notification_number": "WIPO/GRTKF/2024/TREATY",
            "category": "International Treaties",
            "summary": "International consensus on mandatory patent disclosure requirements for genetic resources and traditional knowledge origins.",
            "source_url": "https://www.wipo.int/tk/en/igc/",
            "issued_date": "2024-05-24",
            "status": "International Standard"
        }
    ]

    def get_recent_updates(self, limit: int = 4) -> List[Dict[str, Any]]:
        """Retrieve recent regulatory updates."""
        return self.SEED_UPDATES[:limit]


regulation_monitor = RegulationMonitor()
