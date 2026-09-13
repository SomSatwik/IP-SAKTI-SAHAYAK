package com.ipsakti.sahayak.data.api

import com.ipsakti.sahayak.data.model.*
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
    suspend fun getDemoInvestigation(): Response<InvestigationDetail>

    @POST("api/documents/upload")
    suspend fun uploadDocument(): Response<DocumentUploadResponse>
}
