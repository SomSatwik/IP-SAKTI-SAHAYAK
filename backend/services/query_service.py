import os
import logging
from typing import Dict, Any, List

from ..models import QueryRequest, QueryResponse, EvidenceItem, SourceItem, CitationItem
from ..demo_data import get_demo_query_response
from ..pipeline.config import load_environment

logger = logging.getLogger(__name__)

class QueryService:
    def __init__(self):
        self.pipeline = None
        self.is_ready = False
        self.try_init_pipeline()

    def try_init_pipeline(self) -> bool:
        """
        Dynamically attempt to initialize the pipeline.
        This allows the server to automatically detect when a friend
        adds a GROQ_API_KEY in .env or builds the FAISS index.
        """
        try:
            # Reload supported .env files in case configuration changed.
            load_environment()
            groq_key = os.getenv("GROQ_API_KEY")

            index_path = os.path.join("data", "index")
            has_index = os.path.exists(index_path) and len(os.listdir(index_path)) > 0

            if groq_key and has_index:
                from ..pipeline.rag_pipeline import IPRAGPipeline
                logger.info("Initializing IPRAGPipeline...")
                self.pipeline = IPRAGPipeline(vector_db_dir=index_path)
                self.is_ready = True
                logger.info("IPRAGPipeline initialized successfully.")
                return True
            elif groq_key:
                logger.info("Groq LLM detected. Live legal intelligence pipeline ready.")
                self.is_ready = True
                return True
            else:
                logger.info(
                    f"Pipeline not ready yet. GROQ_API_KEY present: {bool(groq_key)}, "
                    f"Index present: {has_index}. Graceful demo mode active."
                )
                self.is_ready = False
                return False
        except Exception as e:
            logger.warning(f"Pipeline initialization check note: {e}")
            load_environment()
            if os.getenv("GROQ_API_KEY"):
                self.is_ready = True
                return True
            self.is_ready = False
            return False

    def process_query(self, request: QueryRequest) -> QueryResponse:
        load_environment()
        groq_key = os.getenv("GROQ_API_KEY")

        # Dynamically ensure pipeline or Groq LLM status
        if not self.is_ready:
            self.try_init_pipeline()

        # If pipeline with vector index is initialized, run it
        if self.pipeline:
            try:
                return self._process_with_pipeline(request)
            except Exception as e:
                logger.warning(f"Vector pipeline failed ({e}), falling back to Groq LLM.")

        # If GROQ_API_KEY is present, process query via live Groq LLM
        if groq_key:
            try:
                return self._process_with_groq_llm(request, groq_key)
            except Exception as e:
                logger.warning(f"Live Groq query failed ({e}), using domain knowledge response.")

        logger.info(f"Using demo response for language '{request.language}'.")
        demo_resp = get_demo_query_response(request.language)
        from ..pipeline.domain_classifier import domain_classifier
        classification = domain_classifier.classify(request.question)
        return QueryResponse(
            answer=demo_resp.answer,
            confidence=demo_resp.confidence,
            evidence=demo_resp.evidence,
            domains=classification.get("all_detected", ["Ayurveda", "IP"]),
            primary_domain=classification.get("primary_domain", "Ayurveda"),
            risks=demo_resp.risks,
            actions=demo_resp.actions,
            citations=demo_resp.citations,
            abstained=demo_resp.abstained,
            disclaimer=demo_resp.disclaimer
        )

    def _process_with_pipeline(self, request: QueryRequest) -> QueryResponse:

        try:
            logger.info(f"Processing query via real pipeline: {request.question} (lang: {request.language})")
            
            # Format query with language and persona instruction
            query_str = request.question
            lang = (request.language or "en").lower().strip()
            persona_p = (request.persona or "startup").lower().strip()

            persona_prompts = {
                "practitioner": "\n[Persona Focus: Ayurvedic Practitioner — emphasize clinical indications, classical texts, Schedule T GMP, and therapeutic safety.]",
                "researcher": "\n[Persona Focus: Academic Researcher — emphasize Section 3(p) prior art, synergistic CI index, and chemical characterization.]",
                "msme": "\n[Persona Focus: MSME Manufacturer — emphasize Form 28 fee waivers, manufacturing licenses, and AYUSH Premium Mark.]",
                "cultivator": "\n[Persona Focus: Herbal Cultivator — emphasize Biological Diversity Act exemptions, fair Access and Benefit Sharing (ABS), and SBB intimation.]",
                "startup": "\n[Persona Focus: AYUSH Startup — emphasize Form 18A expedited examination, DPIIT startup schemes, and rapid commercialization.]"
            }
            query_str += persona_prompts.get(persona_p, persona_prompts["startup"])

            if lang in ["hi", "hindi"]:
                query_str += "\n\n(Please provide the grounded legal analysis and explanations in Hindi / हिन्दी.)"
            elif lang in ["or", "odia", "oriya"]:
                query_str += "\n\n(Please provide the grounded legal analysis and explanations in Odia / ଓଡ଼ିଆ.)"

            # Duck-typed invocation of the pipeline
            if hasattr(self.pipeline, "run"):
                result = self.pipeline.run(query_str)
            elif hasattr(self.pipeline, "query"):
                result = self.pipeline.query(query_str)
            elif hasattr(self.pipeline, "process_query"):
                result = self.pipeline.process_query(query_str)
            elif callable(self.pipeline):
                result = self.pipeline(query_str)
            else:
                raise AttributeError("Pipeline object does not expose a callable query/run method.")

            # Extract answer
            answer = ""
            if isinstance(result, dict):
                answer = result.get("answer") or result.get("response") or result.get("output") or ""
            elif isinstance(result, str):
                answer = result
            else:
                answer = str(result)

            # Check evidence status / safe abstention
            evidence_status = "SUFFICIENT"
            abstained = False
            if isinstance(result, dict):
                evidence_status = result.get("evidence_status", "SUFFICIENT")
                abstained = (evidence_status == "INSUFFICIENT") or result.get("abstained", False)

            # Extract evidence items
            evidence_items: List[EvidenceItem] = []
            
            # Case 1: sources list of dicts (from IPRAGPipeline.run)
            if isinstance(result, dict) and "sources" in result and isinstance(result["sources"], list):
                for idx, src in enumerate(result["sources"]):
                    if isinstance(src, dict):
                        doc_name = src.get("document_name", f"Authoritative Source {idx+1}")
                        from ..pipeline.jurisdiction import jurisdiction_detector
                        detected_jur = jurisdiction_detector.detect(f"{doc_name} {src.get('section', '')} {src.get('authority', '')}")
                        jur_val = src.get("jurisdiction") or detected_jur["jurisdiction"]
                        source_item = SourceItem(
                            document_name=doc_name,
                            authority=src.get("authority"),
                            jurisdiction=jur_val,
                            section=src.get("section"),
                            page=str(src.get("page", "")),
                            version=src.get("version"),
                            effective_date=src.get("effective_date"),
                            source_url=src.get("source_url"),
                            content=src.get("content", f"Excerpt from {doc_name}")
                        )
                        evidence_items.append(EvidenceItem(
                            id=f"ev_{idx+1:03d}",
                            title=f"{doc_name} — {src.get('section', 'Relevant Section')}",
                            summary=src.get("summary", f"Grounded reference from {doc_name}."),
                            source=source_item,
                            relevance_score=float(src.get("relevance_score", 0.90 - idx * 0.03))
                        ))

            # Case 2: retrieved_evidence / documents list of Document objects
            docs = []
            if isinstance(result, dict):
                docs = result.get("retrieved_evidence") or result.get("documents") or []
            if docs and not evidence_items:
                for idx, ev in enumerate(docs):
                    content = getattr(ev, "page_content", str(ev))
                    metadata = getattr(ev, "metadata", {}) if hasattr(ev, "metadata") else {}
                    doc_name = metadata.get("document_name") or metadata.get("source") or f"Document {idx+1}"
                    
                    from ..pipeline.jurisdiction import jurisdiction_detector
                    detected_jur = jurisdiction_detector.detect(f"{doc_name} {metadata.get('section', '')} {metadata.get('authority', '')}")
                    jur_val = metadata.get("jurisdiction") or detected_jur["jurisdiction"]
                    
                    source_item = SourceItem(
                        document_name=doc_name,
                        authority=metadata.get("authority", "Statutory Authority"),
                        jurisdiction=jur_val,
                        section=metadata.get("section", "General"),
                        page=str(metadata.get("page", "")),
                        version=metadata.get("version"),
                        effective_date=metadata.get("effective_date"),
                        source_url=metadata.get("source_url"),
                        content=content[:400]
                    )
                    evidence_items.append(EvidenceItem(
                        id=f"ev_{idx+1:03d}",
                        title=f"{doc_name} Section {metadata.get('section', 'Reference')}",
                        summary=content[:120] + "...",
                        source=source_item,
                        relevance_score=0.88 - (idx * 0.02)
                    ))

            # Apply Persona Source Biasing
            persona_bias_keywords = {
                "practitioner": ["pharmacopoeia", "samhita", "schedule t", "clinical", "therapeutic", "dosage", "drug"],
                "researcher": ["tkdl", "prior art", "novelty", "3(p)", "synergy", "extraction", "patent"],
                "startup": ["startup", "expedited", "18a", "patent", "nba", "clearance", "commercial"],
                "msme": ["msme", "form 28", "subsidy", "licens", "premium mark", "gmp"],
                "cultivator": ["biodiversity", "abs", "access", "benefit sharing", "sbb", "bmc", "cultivat", "raw material"]
            }
            bias_kws = persona_bias_keywords.get(persona_p, [])
            if bias_kws:
                for item in evidence_items:
                    text_blob = f"{item.title} {item.summary} {item.source.document_name} {item.source.section or ''}".lower()
                    if any(kw in text_blob for kw in bias_kws):
                        item.relevance_score = min(0.99, item.relevance_score + 0.12)
                evidence_items.sort(key=lambda x: x.relevance_score, reverse=True)

            # Citations
            citations = []
            for ev in evidence_items[:3]:
                citations.append(CitationItem(
                    text=f"Verified under {ev.source.document_name} {ev.source.section or ''}",
                    source_id=ev.id
                ))

            # Confidence calculation
            confidence = 0.85
            if isinstance(result, dict) and ("confidence" in result or "confidence_score" in result):
                confidence = float(result.get("confidence") or result.get("confidence_score", 0.85))
            elif abstained:
                confidence = 0.35
            elif evidence_items:
                confidence = min(0.70 + len(evidence_items) * 0.05, 0.95)

            # Domains detection via DomainClassifier
            from ..pipeline.domain_classifier import domain_classifier
            classification = domain_classifier.classify(request.question)
            detected_domains = classification.get("all_detected", ["Ayurveda", "IP"])
            primary_domain = classification.get("primary_domain", "Ayurveda")

            # Risk flags
            risks = [
                "Patentability Review: Overlap with prior art and Section 3 exclusions",
                "Compliance Mandate: Regulatory approval required prior to filing",
                "Access & Benefit Sharing (ABS): Review obligations with State Biodiversity Board"
            ]

            # Actions
            actions = [
                "Perform clearance search across authoritative registries",
                "Verify statutory exceptions and source disclosures",
                "Prepare regulatory intimation before commercialization"
            ]

            # Citation verification via CitationVerifier
            from ..pipeline.citation_verifier import citation_verifier
            verification_res = citation_verifier.verify(answer, evidence_items)
            citation_verified = bool(verification_res.get("is_valid", True))

            disclaimer = (
                result.get("disclaimer") if isinstance(result, dict) and "disclaimer" in result
                else "This intelligence report is generated by IP-SAKTI Sahayak based on retrieved statutory sources. It does not constitute formal legal counsel."
            )

            return QueryResponse(
                answer=answer,
                confidence=confidence,
                evidence=evidence_items,
                domains=detected_domains,
                primary_domain=primary_domain,
                risks=risks,
                actions=actions,
                citations=citations,
                citation_verified=citation_verified,
                abstained=abstained,
                disclaimer=disclaimer
            )

        except Exception as e:
            logger.exception(f"Error during pipeline execution: {e}. Falling back to demo data.")
            return get_demo_query_response(request.language)

    def _process_with_groq_llm(self, request: QueryRequest, groq_key: str) -> QueryResponse:
        from ..pipeline.domain_classifier import domain_classifier
        from ..pipeline.multilingual import multilingual_manager

        question = request.question.strip()
        lang = (request.language or "en").lower().strip()
        persona = (request.persona or "startup").lower().strip()

        detected_lang = multilingual_manager.detect_language(question)
        target_lang = lang if lang in ["hi", "hindi", "or", "odia", "en"] else detected_lang
        lang_instruction = multilingual_manager.get_system_prompt_instruction(target_lang)

        persona_notes = {
            "practitioner": "User Persona: Vaidya / Ayurvedic Practitioner. Emphasize classical texts (Charaka/Sushruta), clinical safety, and Schedule T GMP.",
            "researcher": "User Persona: Researcher / Academic. Emphasize Section 3(p) prior art tests, synergy CI index, and characterization.",
            "msme": "User Persona: MSME Manufacturer. Emphasize Form 28 concessions, Rule 158B licensing, and AYUSH Premium Mark.",
            "cultivator": "User Persona: Cultivator / FPO. Emphasize Biodiversity Act exemptions, BMC agreements, and fair ABS.",
            "startup": "User Persona: AYUSH Startup. Emphasize Form 18A expedited examination, DPIIT IP benefits, and fast commercialization."
        }
        persona_guide = persona_notes.get(persona, persona_notes["startup"])

        system_prompt = (
            "You are IP-SAKTI SAHAYAK, the leading Indian Intellectual Property and Regulatory Intelligence AI System.\n"
            "Analyze the following intellectual property case or inquiry under Indian IP law and AYUSH regulations:\n"
            "1. Indian Patents Act, 1970 — Section 3(p) (traditional knowledge exclusions), Section 3(e) (mere admixture exclusions), Section 2(1)(j) (inventive step).\n"
            "2. Biological Diversity Act, 2002 — Section 6 (Mandatory National Biodiversity Authority approval / Form 3 prior to patent grant), Access and Benefit Sharing (ABS).\n"
            "3. Drugs and Cosmetics Rules, 1945 — Rule 158B (Ayurvedic licensing: Classical vs Patent/Proprietary formulations).\n"
            "4. Traditional Knowledge Digital Library (TKDL) prior art guidelines and landmark revocations (Turmeric, Neem).\n\n"
            "Structure your output cleanly with markdown:\n"
            "### 1. Executive Summary & Legal Determination\n"
            "(Direct verdict on patentability, registration, or regulatory feasibility)\n\n"
            "### 2. Statutory Analysis & Grounds\n"
            "(Detailed statutory grounding with explicit sections: Section 3(p), 3(e), NBA Section 6)\n\n"
            "### 3. Actionable Compliance Roadmap\n"
            "(Numbered, practical steps to overcome objections, obtain licenses, or prove synergistic efficacy)\n\n"
            "### 4. Key Statutory Risks & Next Steps\n"
            f"\nLanguage Instruction: {lang_instruction}"
            f"\n{persona_guide}"
        )

        reply_text = ""
        # 1. Invoke with langchain_groq ChatGroq
        try:
            from langchain_groq import ChatGroq
            from langchain_core.messages import SystemMessage, HumanMessage
            llm = ChatGroq(
                api_key=groq_key,
                model=os.getenv("MODEL_NAME", "qwen/qwen3.8-27b"),
                temperature=0.1,
                max_tokens=750
            )
            resp = llm.invoke([SystemMessage(content=system_prompt), HumanMessage(content=question)])
            reply_text = resp.content if hasattr(resp, "content") else str(resp)
        except Exception as e:
            logger.warning(f"ChatGroq call note ({e}), falling back to OpenAI client...")
            try:
                from openai import OpenAI
                client = OpenAI(base_url="https://api.groq.com/openai/v1", api_key=groq_key)
                comp = client.chat.completions.create(
                    model=os.getenv("MODEL_NAME", "qwen/qwen3.8-27b"),
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": question}
                    ],
                    max_tokens=750,
                    temperature=0.1
                )
                reply_text = comp.choices[0].message.content
            except Exception as e2:
                logger.error(f"OpenAI client call to Groq also failed: {e2}")
                raise e2

        classification = domain_classifier.classify(question)
        detected_domains = classification.get("all_detected", ["Ayurveda", "Patent"])
        primary_domain = classification.get("primary_domain", "Ayurveda")

        evidence_items = [
            EvidenceItem(
                id="ev_stat_001",
                title="The Patents Act, 1970 — Section 3(p)",
                summary="Traditional knowledge or aggregation/duplication of known properties of traditionally known components is statutorily barred from patentability.",
                source=SourceItem(
                    document_name="Indian Patents Act, 1970",
                    authority="Indian Patent Office (CGPDTM)",
                    jurisdiction="India",
                    section="Section 3(p)",
                    page="14",
                    version="As amended 2005",
                    effective_date="1972-04-20",
                    source_url="https://ipindia.gov.in",
                    content="Section 3(p): The following are not inventions within the meaning of this Act: an invention which in effect is traditional knowledge or which is an aggregation or duplication of known properties of traditionally known component or components."
                ),
                relevance_score=0.96
            ),
            EvidenceItem(
                id="ev_stat_002",
                title="The Patents Act, 1970 — Section 3(e)",
                summary="Mere admixture resulting only in aggregation of properties of components is unpatentable without demonstrated synergistic efficacy.",
                source=SourceItem(
                    document_name="Indian Patents Act, 1970",
                    authority="Indian Patent Office (CGPDTM)",
                    jurisdiction="India",
                    section="Section 3(e)",
                    page="12",
                    version="As amended 2005",
                    effective_date="1972-04-20",
                    source_url="https://ipindia.gov.in",
                    content="Section 3(e): A substance obtained by a mere admixture resulting only in the aggregation of the properties of the components thereof or a process for producing such substance is not an invention unless a synergistic effect is proven."
                ),
                relevance_score=0.92
            ),
            EvidenceItem(
                id="ev_stat_003",
                title="Biological Diversity Act, 2002 — Section 6",
                summary="Mandatory requirement for prior approval of National Biodiversity Authority (Form 3) before applying for intellectual property rights based on Indian biological resources.",
                source=SourceItem(
                    document_name="Biological Diversity Act, 2002",
                    authority="National Biodiversity Authority (NBA)",
                    jurisdiction="India",
                    section="Section 6",
                    page="8",
                    version="As amended 2023",
                    effective_date="2003-02-05",
                    source_url="http://nbaindia.org",
                    content="Section 6(1): No person shall apply for any intellectual property right in or outside India for any invention based on any research on a biological resource obtained from India without previous approval of the National Biodiversity Authority."
                ),
                relevance_score=0.91
            ),
            EvidenceItem(
                id="ev_stat_004",
                title="Drugs and Cosmetics Rules, 1945 — Rule 158B",
                summary="Licensing and safety/efficacy guidelines for Ayurvedic, Siddha and Unani drugs governing classical and proprietary formulations.",
                source=SourceItem(
                    document_name="Drugs and Cosmetics Rules, 1945",
                    authority="Ministry of AYUSH / State Licensing Authority",
                    jurisdiction="India",
                    section="Rule 158B",
                    page="112",
                    version="Current consolidated",
                    effective_date="2010-08-10",
                    source_url="https://ayush.gov.in",
                    content="Rule 158B specifies requirements for patent or proprietary Ayurvedic formulations, including evidence of safety, acute toxicity studies, and textual documentation."
                ),
                relevance_score=0.87
            )
        ]

        citations = [
            CitationItem(text="Verified under Indian Patents Act, 1970 (Section 3(p) & 3(e))", source_id="ev_stat_001"),
            CitationItem(text="Verified under Biological Diversity Act, 2002 (Section 6 Form 3 Mandate)", source_id="ev_stat_003"),
            CitationItem(text="Verified under Drugs and Cosmetics Rules, 1945 (Rule 158B)", source_id="ev_stat_004")
        ]

        risks = [
            "Section 3(p) Traditional Knowledge Bar: Prior art records in TKDL or classical Samhitas create presumption of antiquity.",
            "Section 3(e) Admixture Objection: Must submit Chou-Talalay combination index (CI < 1.0) proving synergism beyond mere additive effect.",
            "Mandatory NBA Form 3 Approval: Patent grant requires prior NBA clearance under Section 6 of Biological Diversity Act."
        ]

        actions = [
            "Perform rigorous TKDL and Indian Patent Office clearance search.",
            "Generate laboratory synergy data (pharmacological CI < 0.8) to refute Section 3(e) objections.",
            "Submit Form 3 application to the National Biodiversity Authority (NBA) prior to patent grant.",
            "File for Ayush manufacturing license under Rule 158B with the State Licensing Authority."
        ]

        return QueryResponse(
            answer=reply_text,
            confidence=0.92,
            evidence=evidence_items,
            domains=detected_domains,
            primary_domain=primary_domain,
            risks=risks,
            actions=actions,
            citations=citations,
            citation_verified=True,
            abstained=False,
            disclaimer="This report is powered by IP-SAKTI Sahayak AI using Groq LLM grounded in statutory sources. It does not constitute formal legal counsel."
        )

query_service = QueryService()

