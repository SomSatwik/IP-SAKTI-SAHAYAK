package com.ipsakti.sahayak.data.model

import com.google.gson.annotations.SerializedName

data class DiffSegment(
    val text: String,
    val type: String // "unchanged", "added", "removed"
)

data class TimelineVersion(
    @SerializedName("version_title") val versionTitle: String,
    @SerializedName("amendment_act") val amendmentAct: String,
    @SerializedName("effective_date") val effectiveDate: String,
    val status: String,
    val summary: String,
    @SerializedName("diff_segments") val diffSegments: List<DiffSegment>
)

data class RegulationTimeline(
    @SerializedName("source_id") val sourceId: String,
    @SerializedName("document_name") val documentName: String,
    val section: String,
    val versions: List<TimelineVersion>
)
