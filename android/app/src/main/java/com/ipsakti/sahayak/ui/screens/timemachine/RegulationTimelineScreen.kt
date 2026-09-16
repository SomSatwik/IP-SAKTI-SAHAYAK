package com.ipsakti.sahayak.ui.screens.timemachine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.model.RegulationTimeline
import com.ipsakti.sahayak.data.model.TimelineVersion
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun RegulationTimelineScreen(
    sourceId: String,
    onNavigateBack: () -> Unit,
    repository: IpSaktiRepository = remember { IpSaktiRepository() }
) {
    var timeline by remember { mutableStateOf<RegulationTimeline?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(sourceId) {
        isLoading = true
        val result = repository.getRegulationTimeline(sourceId)
        timeline = result.getOrNull()
        isLoading = false
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Regulation Time Machine",
                subtitle = "Statutory Evolution & Legislative Diff",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RoyalBlue800)
            }
        } else {
            val data = timeline
            if (data == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No version history found for source: $sourceId")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Overview Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Navy900),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = Gold600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "LEGISLATIVE TIMELINE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Gold600,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = RoyalBlue800.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "${data.versions.size} Amendments",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = data.documentName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Target Provision: ${data.section}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate300,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Diff Legend Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate100),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(RiskHigh)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Repealed / Omitted",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RiskHigh,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(RiskLow)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Inserted / Amended",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RiskLow,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Vertical Timeline Version Items
                    itemsIndexed(data.versions) { index, version ->
                        TimelineVersionCard(
                            version = version,
                            index = index,
                            isLast = index == data.versions.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineVersionCard(
    version: TimelineVersion,
    index: Int,
    isLast: Boolean
) {
    val isInForce = version.status.contains("In Force", ignoreCase = true)
    val statusColor = if (isInForce) RiskLow else Slate500
    val statusBg = if (isInForce) RiskLowBg else Slate200

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical Timeline Column (Node + Connecting Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isInForce) RoyalBlue800 else Slate400),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(180.dp)
                        .background(Slate300)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Version Content Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(
                width = if (isInForce) 1.5.dp else 1.dp,
                color = if (isInForce) RoyalBlue800.copy(alpha = 0.4f) else CardBorder
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = version.versionTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = version.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = version.amendmentAct,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RoyalBlue800,
                        fontWeight = FontWeight.Medium
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Effective: ${version.effectiveDate}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = version.summary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Slate600,
                        lineHeight = 19.sp,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "STATUTORY PROVISION DIFF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Inline Diff Rendering (Removed struck through in RiskHigh, Added in RiskLowBg)
                val annotatedDiff = buildAnnotatedString {
                    version.diffSegments.forEach { segment ->
                        when (segment.type.lowercase()) {
                            "removed" -> {
                                withStyle(
                                    SpanStyle(
                                        color = RiskHigh,
                                        textDecoration = TextDecoration.LineThrough,
                                        fontWeight = FontWeight.Medium
                                    )
                                ) {
                                    append(segment.text)
                                }
                            }
                            "added" -> {
                                withStyle(
                                    SpanStyle(
                                        color = RiskLow,
                                        background = RiskLowBg,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append(segment.text)
                                }
                            }
                            else -> {
                                withStyle(
                                    SpanStyle(
                                        color = Navy900,
                                        fontWeight = FontWeight.Normal
                                    )
                                ) {
                                    append(segment.text)
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceMuted, RoundedCornerShape(6.dp))
                        .border(BorderStroke(1.dp, Slate200), RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = annotatedDiff,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}
