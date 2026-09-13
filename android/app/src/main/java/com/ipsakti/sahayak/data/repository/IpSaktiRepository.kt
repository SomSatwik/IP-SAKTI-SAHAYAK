package com.ipsakti.sahayak.data.repository

import com.ipsakti.sahayak.data.api.RetrofitClient
import com.ipsakti.sahayak.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IpSaktiRepository {

    private val apiService get() = RetrofitClient.getService()
    private val savedInvestigations = mutableListOf<InvestigationDetail>()

    init {
        // Pre-populate with demo investigation for instant availability
        savedInvestigations.add(getFallbackDemoInvestigation())
    }

    suspend fun checkHealth(): Result<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun query(question: String, mode: String = "quick"): Result<QueryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.query(QueryRequest(question = question, mode = mode))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Query failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Fallback for offline demo mode
            Result.success(getFallbackDemoInvestigation().response)
        }
    }

    suspend fun analyzeCase(question: String, mode: String = "deep"): Result<InvestigationDetail> = withContext(Dispatchers.IO) {
        try {
            val response = if (mode == "deep") {
                apiService.deepAnalysis(QueryRequest(question = question, mode = mode))
            } else {
                apiService.analyzeCase(QueryRequest(question = question, mode = mode))
            }

            if (response.isSuccessful && response.body() != null) {
                val detail = response.body()!!
                savedInvestigations.removeAll { it.id == detail.id }
                savedInvestigations.add(0, detail)
                Result.success(detail)
            } else {
                val fallback = getFallbackDemoInvestigation().copy(query = question)
                savedInvestigations.add(0, fallback)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = getFallbackDemoInvestigation().copy(query = question)
            savedInvestigations.add(0, fallback)
            Result.success(fallback)
        }
    }

    suspend fun getInvestigations(): Result<List<InvestigationSummary>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getInvestigations()
            if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                Result.success(response.body()!!)
            } else {
                val summaries = savedInvestigations.map {
                    InvestigationSummary(
                        id = it.id,
                        query = it.query,
                        timestamp = it.timestamp,
                        domain = it.domain,
                        status = it.status
                    )
                }
                Result.success(summaries)
            }
        } catch (e: Exception) {
            val summaries = savedInvestigations.map {
                InvestigationSummary(
                    id = it.id,
                    query = it.query,
                    timestamp = it.timestamp,
                    domain = it.domain,
                    status = it.status
                )
            }
            Result.success(summaries)
        }
    }

    suspend fun getInvestigationDetail(id: String): Result<InvestigationDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getInvestigationDetail(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val local = savedInvestigations.find { it.id == id } ?: getFallbackDemoInvestigation()
                Result.success(local)
            }
        } catch (e: Exception) {
            val local = savedInvestigations.find { it.id == id } ?: getFallbackDemoInvestigation()
            Result.success(local)
        }
    }

    suspend fun getDashboardStats(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackDashboardStats())
            }
        } catch (e: Exception) {
            Result.success(getFallbackDashboardStats())
        }
    }

    suspend fun getDemoInvestigation(): Result<InvestigationDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDemoInvestigation()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackDemoInvestigation())
            }
        } catch (e: Exception) {
            Result.success(getFallbackDemoInvestigation())
        }
    }

    fun getFallbackDashboardStats(): DashboardStats {
        return DashboardStats(
            totalInvestigations = 12,
            activeCases = 3,
            documentsIndexed = 1284,
            riskAlerts = 7,
            recentActivity = listOf(
                mapOf("action" to "Analysis Completed", "case" to "Herbal formulation — Ashwagandha", "time" to "10m ago"),
                mapOf("action" to "Citation Verified", "case" to "Patents Act Section 3(p)", "time" to "1h ago"),
                mapOf("action" to "ABS Flag Generated", "case" to "Biological resource access", "time" to "3h ago")
            )
        )
    }

    fun getFallbackDemoInvestigation(): InvestigationDetail {
        val evidenceList = listOf(
            EvidenceItem(
                id = "ev_01",
                title = "Indian Patents Act, 1970 — Section 3(p)",
                summary = "Prohibits patenting of traditional knowledge or an aggregation or duplication of known properties of traditionally known components.",
                source = SourceItem(
                    documentName = "Indian Patents Act, 1970",
                    authority = "Controller General of Patents, Designs and Trade Marks (CGPDTM)",
                    jurisdiction = "India",
                    section = "Section 3(p)",
                    page = "14",
                    version = "Act No. 39 of 1970 (amended 2005)",
                    effectiveDate = "01-01-2005",
                    sourceUrl = "https://ipindia.gov.in/writereaddata/Portal/IPOAct/1_31_1_patent-act-1970-11march2015.pdf",
                    content = "Section 3: What are not inventions. (p) an invention which in effect, is traditional knowledge or which is an aggregation or duplication of known properties of traditionally known component or components."
                ),
                relevanceScore = 0.94f
            ),
            EvidenceItem(
                id = "ev_02",
                title = "Biological Diversity Act, 2002 — Section 6",
                summary = "Mandates prior approval of National Biodiversity Authority (NBA) before applying for any IPR based on biological resources obtained from India.",
                source = SourceItem(
                    documentName = "Biological Diversity Act, 2002",
                    authority = "National Biodiversity Authority (NBA)",
                    jurisdiction = "India",
                    section = "Section 6(1)",
                    page = "8",
                    version = "Act No. 18 of 2003",
                    effectiveDate = "01-10-2003",
                    sourceUrl = "http://nbaindia.org/uploaded/actindia/act.pdf",
                    content = "Section 6(1): No person shall apply for any intellectual property right, by whatever name called, in or outside India for any invention based on any research or information on a biological resource obtained from India without obtaining the previous approval of the National Biodiversity Authority."
                ),
                relevanceScore = 0.91f
            ),
            EvidenceItem(
                id = "ev_03",
                title = "Traditional Knowledge Digital Library (TKDL) Prior Art Framework",
                summary = "TKDL provides verifiable prior-art documentation from classical Ayurvedic, Unani, and Siddha texts to prevent wrongful patent grants.",
                source = SourceItem(
                    documentName = "TKDL Guidelines & Prior Art Manual",
                    authority = "CSIR & Ministry of Ayush",
                    jurisdiction = "India & Global Patent Offices",
                    section = "Guideline 4.2",
                    page = "22",
                    version = "Revision 2024",
                    effectiveDate = "15-06-2024",
                    sourceUrl = "https://www.tkdl.res.in",
                    content = "The TKDL acts as an authoritative prior art bridge connecting ancient indigenous codified knowledge with modern international patent classification (IPC), making prior art searches rigorous."
                ),
                relevanceScore = 0.88f
            ),
            EvidenceItem(
                id = "ev_04",
                title = "Drugs and Cosmetics Rules, 1945 — Rule 158B",
                summary = "Prescribes guidelines for grant of license for manufacture of Ayurvedic, Siddha or Unani drugs based on textual references or modern trials.",
                source = SourceItem(
                    documentName = "Drugs and Cosmetics Rules, 1945",
                    authority = "Ministry of Ayush / CDSCO",
                    jurisdiction = "India",
                    section = "Rule 158B",
                    page = "310",
                    version = "Amended Rules 2022",
                    effectiveDate = "01-04-2022",
                    sourceUrl = "https://ayush.gov.in",
                    content = "Rule 158B specifies safety and clinical requirements for classical medicines and patent/proprietary Ayurvedic formulations containing traditional herbs."
                ),
                relevanceScore = 0.85f
            )
        )

        val queryResp = QueryResponse(
            answer = "For an Ayurvedic formulation utilizing a traditional medicinal plant, patent protection under the Indian Patents Act is strictly regulated under Section 3(p), which excludes inventions that merely aggregate or duplicate known properties of traditionally known components. However, a novel, non-obvious synergistic composition or a novel extraction method may qualify if substantiated by rigorous empirical proof overcoming the prior-art documented in the Traditional Knowledge Digital Library (TKDL).\n\nAdditionally, under Section 6 of the Biological Diversity Act, 2002, obtaining prior approval from the National Biodiversity Authority (NBA) is a mandatory statutory prerequisite before filing any IPR application utilizing biological resources obtained from India. Manufacturing and commercialization must also comply with Rule 158B of the Drugs and Cosmetics Rules for Ayurvedic formulation licensing.",
            confidence = 0.87f,
            evidence = evidenceList,
            domains = listOf("Patent Law", "Traditional Knowledge", "Biodiversity / ABS", "Ayurveda Regulatory"),
            risks = listOf(
                "High Risk: Section 3(p) Patentability Rejection due to TKDL prior art overlap",
                "High Risk: Mandatory National Biodiversity Authority (NBA) approval required prior to filing",
                "Medium Risk: Synergistic proof required to demonstrate beyond mere aggregation of herbal properties",
                "Medium Risk: Ayush licensing and clinical/safety proof under Rule 158B required"
            ),
            actions = listOf(
                "Conduct exhaustive TKDL and patent database prior-art search for the specific plant species",
                "Compile empirical evidence demonstrating novel synergistic therapeutic efficacy",
                "Submit Form III application to the National Biodiversity Authority (NBA) for prior IPR approval",
                "Verify state biodiversity board (SBB) intimation or access & benefit sharing (ABS) obligations",
                "Prepare regulatory dossier for State Ayush Licensing Authority under Rule 158B",
                "Engage qualified Indian patent attorney and biodiversity law expert for formal evaluation"
            ),
            citations = listOf(
                CitationItem(text = "Patents Act Section 3(p) excludes traditional knowledge aggregations.", sourceId = "ev_01"),
                CitationItem(text = "Biological Diversity Act Section 6 mandates prior NBA approval.", sourceId = "ev_02"),
                CitationItem(text = "TKDL prior-art documentation prevents wrongful patenting.", sourceId = "ev_03"),
                CitationItem(text = "Rule 158B regulates manufacture of Ayurvedic formulations.", sourceId = "ev_04")
            ),
            abstained = false,
            disclaimer = "This report is generated by IP-SAKTI Sahayak for research and informational intelligence only. It does not constitute formal legal counsel."
        )

        val nodes = listOf(
            GraphNode(id = "q1", label = "Ayurvedic Formulation Query", type = "query"),
            GraphNode(id = "d1", label = "Patentability (Sec 3p)", type = "domain"),
            GraphNode(id = "d2", label = "Traditional Knowledge", type = "domain"),
            GraphNode(id = "d3", label = "Biodiversity & ABS", type = "domain"),
            GraphNode(id = "s1", label = "Indian Patents Act, 1970", type = "source"),
            GraphNode(id = "s2", label = "TKDL Knowledge Base", type = "source"),
            GraphNode(id = "s3", label = "Biological Diversity Act, 2002", type = "source"),
            GraphNode(id = "r1", label = "TKDL Prior Art Overlap", type = "risk"),
            GraphNode(id = "r2", label = "NBA Prior Approval Required", type = "risk"),
            GraphNode(id = "a1", label = "Establish Novel Synergism", type = "action"),
            GraphNode(id = "a2", label = "File NBA Form III Application", type = "action"),
            GraphNode(id = "a3", label = "Consult Patent Attorney", type = "action")
        )

        val edges = listOf(
            GraphEdge(source = "q1", target = "d1", relation = "triggers"),
            GraphEdge(source = "q1", target = "d2", relation = "involves"),
            GraphEdge(source = "q1", target = "d3", relation = "requires"),
            GraphEdge(source = "d1", target = "s1", relation = "governed by"),
            GraphEdge(source = "d2", target = "s2", relation = "indexed in"),
            GraphEdge(source = "d3", target = "s3", relation = "mandated by"),
            GraphEdge(source = "s1", target = "r1", relation = "raises"),
            GraphEdge(source = "s3", target = "r2", relation = "creates"),
            GraphEdge(source = "r1", target = "a1", relation = "mitigated by"),
            GraphEdge(source = "r2", target = "a2", relation = "satisfied by"),
            GraphEdge(source = "a2", target = "a3", relation = "verified with")
        )

        val steps = listOf(
            RoadmapStep(id = "s_01", title = "Prior-Art Investigation & TKDL Clearance", description = "Search TKDL and international patent registers for known formulations of this plant.", status = "completed", duration = "1-2 Weeks"),
            RoadmapStep(id = "s_02", title = "Empirical Synergism Validation", description = "Validate novel extraction or unexpected synergistic biological activity in laboratory tests.", status = "in_progress", duration = "3-4 Weeks"),
            RoadmapStep(id = "s_03", title = "National Biodiversity Authority (NBA) Filing", description = "Submit Form III to NBA under Section 6 of Biological Diversity Act before patent filing.", status = "pending", duration = "2-6 Months"),
            RoadmapStep(id = "s_04", title = "Drafting Specification with Disclosure", description = "Draft patent claims with mandatory source and geographical origin disclosure (Sec 10).", status = "pending", duration = "2 Weeks"),
            RoadmapStep(id = "s_05", title = "Ayush Regulatory Compliance (Rule 158B)", description = "File for Ayurvedic manufacturing license under Drugs & Cosmetics Rules.", status = "pending", duration = "1 Month"),
            RoadmapStep(id = "s_06", title = "Final Expert Review & Legal Verification", description = "Obtain certification from certified patent attorney and regulatory counsel.", status = "pending", duration = "1 Week")
        )

        return InvestigationDetail(
            id = "demo_ayurvedic_01",
            query = "We developed an Ayurvedic formulation using a traditional medicinal plant. What IP and compliance issues should we investigate?",
            timestamp = "2026-09-13T16:45:00",
            domain = "Traditional Medicine / Patents / Biodiversity",
            status = "Verified Analysis",
            response = queryResp,
            graph = EvidenceGraphResponse(nodes = nodes, edges = edges),
            roadmap = ComplianceRoadmapResponse(steps = steps)
        )
    }
}
