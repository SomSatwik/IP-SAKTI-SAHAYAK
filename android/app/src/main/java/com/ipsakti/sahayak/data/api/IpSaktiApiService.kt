package com.ipsakti.sahayak.data.api

import com.ipsakti.sahayak.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface IpSaktiApiService {

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("api/query")
    suspend fun query(@Body request: QueryRequest): Response<QueryResponse>

    @POST("api/analyze")
    suspend fun analyzeCase(@Body request: QueryRequest): Response<InvestigationDetail>

    @POST("api/deep-analysis")
    suspend fun deepAnalysis(@Body request: QueryRequest): Response<InvestigationDetail>

    @GET("api/investigations")
    suspend fun getInvestigations(): Response<List<InvestigationSummary>>

    @GET("api/investigations/{id}")
    suspend fun getInvestigationDetail(@Path("id") id: String): Response<InvestigationDetail>

    @GET("api/evidence/{id}")
    suspend fun getEvidence(@Path("id") id: String): Response<List<EvidenceItem>>

    @GET("api/graph/{id}")
    suspend fun getGraph(@Path("id") id: String): Response<EvidenceGraphResponse>

    @GET("api/roadmap/{id}")
    suspend fun getRoadmap(@Path("id") id: String): Response<ComplianceRoadmapResponse>

    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStats>

    @GET("api/demo/investigation")
    suspend fun getDemoInvestigation(@Query("lang") lang: String? = null): Response<InvestigationDetail>

    @Multipart
    @POST("api/documents/upload")
    suspend fun uploadDocumentFile(@Part file: MultipartBody.Part): Response<DocumentUploadResponse>

    @GET("api/documents")
    suspend fun getDocuments(): Response<List<DocumentInfo>>

    @POST("api/chat")
    suspend fun sendChatMessage(@Body request: ChatMessageRequest): Response<ChatResponse>

    @GET("api/timeline/{id}")
    suspend fun getRegulationTimeline(@Path("id") id: String): Response<RegulationTimeline>

    @POST("api/prior-art/search")
    suspend fun searchPriorArt(@Body request: PriorArtSearchRequest): Response<PriorArtSearchResponse>

    @GET("api/compliance/report/{id}")
    suspend fun getComplianceReport(@Path("id") id: String): Response<ComplianceReportResponse>

    @GET("api/regulations/recent")
    suspend fun getRecentRegulations(): Response<List<RegulationUpdate>>

    @GET("api/craft/apothecary")
    suspend fun getApothecary(): Response<List<BotanicalHerb>>

    @POST("api/craft/combine")
    suspend fun combineCraft(@Body request: CraftCombineRequest): Response<CraftCombineResponse>
}
