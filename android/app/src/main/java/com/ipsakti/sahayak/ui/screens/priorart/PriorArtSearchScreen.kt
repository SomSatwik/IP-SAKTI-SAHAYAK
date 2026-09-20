package com.ipsakti.sahayak.ui.screens.priorart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.model.BotanicalInfo
import com.ipsakti.sahayak.data.model.PatentRecord
import com.ipsakti.sahayak.data.model.PriorArtSearchResponse
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

private val QUICK_SUGGESTIONS = listOf(
    "Ashwagandha (Withania somnifera)",
    "Turmeric (Curcumin)",
    "Neem (Azadirachta indica)",
    "Tulsi (Holy Basil)",
    "Triphala Admixture",
    "Guduchi (Giloy)"
)

@Composable
fun PriorArtSearchScreen(
    onNavigateBack: () -> Unit,
    repository: IpSaktiRepository = remember { IpSaktiRepository() },
    initialQuery: String = ""
) {
    val cleanInitial = remember(initialQuery) {
        initialQuery.trim().takeIf {
            it.isNotEmpty() && !it.equals("{initialQuery}", ignoreCase = true) && !it.equals("{initial query}", ignoreCase = true)
        } ?: ""
    }
    var queryText by remember { mutableStateOf(cleanInitial.ifEmpty { "Ashwagandha + Curcumin synergistic formulation" }) }
    var isLoading by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<PriorArtSearchResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun performSearch(query: String) {
        if (query.trim().isEmpty() || isLoading) return
        isLoading = true
        coroutineScope.launch {
            val result = repository.searchPriorArt(query.trim())
            result.onSuccess {
                searchResult = it
                isLoading = false
            }.onFailure {
                searchResult = repository.getFallbackPriorArtResponse(query.trim())
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (searchResult == null) {
            performSearch(queryText)
        }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Prior Art Patent Search",
                subtitle = "Formulation & Botanical Patentability Clearance",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Search Input Box (Reusing InvestigateScreen Card pattern)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SEARCH FORMULATION OR BOTANICAL INGREDIENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "e.g. Ashwagandha extract, Turmeric topical wound healing, Tulsi phytosome...",
                                    color = Slate400,
                                    fontSize = 13.sp
                                )
                            },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue800,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = Slate50,
                                unfocusedContainerColor = Slate50
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quick Botanical Presets:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                            )
                            Button(
                                onClick = { performSearch(queryText) },
                                enabled = queryText.trim().isNotEmpty() && !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Search Prior Art")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(QUICK_SUGGESTIONS) { suggestion ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Slate100,
                                    border = BorderStroke(1.dp, Slate300),
                                    modifier = Modifier.clickable {
                                        queryText = suggestion
                                        performSearch(suggestion)
                                    }
                                ) {
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Navy900,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Section
            searchResult?.let { res ->
                // Summary Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PRIOR ART SEARCH RESULTS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate500,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = "${res.totalFound} Patents Identified",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (res.totalFound > 0) Color(0xFFDC2626).copy(alpha = 0.12f) else Color(0xFF16A34A).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, if (res.totalFound > 0) Color(0xFFDC2626).copy(alpha = 0.3f) else Color(0xFF16A34A).copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = if (res.totalFound > 0) "Prior Art Found" else "No Direct Conflicts",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (res.totalFound > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if (res.detectedBotanicals.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Botanicals Evaluated Against Traditional Knowledge:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Navy800
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    res.detectedBotanicals.forEach { bot ->
                                        BotanicalItemRow(bot)
                                    }
                                }
                            }
                        }
                    }
                }

                // Patent Cards List
                item {
                    Text(
                        text = "EXISTING PATENTS & TKDL PRECEDENTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                }

                items(res.patents) { patent ->
                    PatentCardItem(patent)
                }

                // Section 3(p) / 3(e) Patentability Barriers Card
                if (res.patentabilityBarriers.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = RiskHighBg),
                            border = BorderStroke(1.dp, RiskHighBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = RiskHigh,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Statutory Patentability Hurdles (Indian Patents Act)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RiskHigh
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                res.patentabilityBarriers.forEach { barrier ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = "• ${barrier["statute"]} [${barrier["risk_level"]}]:",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Navy900
                                            )
                                        )
                                        Text(
                                            text = barrier["explanation"] ?: "",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotanicalItemRow(bot: BotanicalInfo) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Slate100,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bot.name} (${bot.scientificName})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        fontSize = 13.sp
                    )
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Gold600.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "TKDL Indexed",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Gold800,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Classical Texts: ${bot.classicalTexts}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
            )
            Text(
                text = "Traditional Use: ${bot.traditionalUses}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
            )
        }
    }
}

@Composable
private fun PatentCardItem(patent: PatentRecord) {
    val statusBg: Color
    val statusTextColor: Color
    val statusBorder: Color

    when {
        patent.status.contains("Revoked", ignoreCase = true) -> {
            statusBg = RiskHighBg
            statusTextColor = RiskHigh
            statusBorder = RiskHighBorder
        }
        patent.status.contains("Granted", ignoreCase = true) -> {
            statusBg = RiskLowBg
            statusTextColor = RiskLow
            statusBorder = RiskLowBorder
        }
        patent.status.contains("Opposed", ignoreCase = true) -> {
            statusBg = RiskHighBg
            statusTextColor = Color(0xFFC2410C)
            statusBorder = RiskHighBorder
        }
        else -> {
            statusBg = RiskMediumBg
            statusTextColor = RiskMedium
            statusBorder = RiskMediumBorder
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Patent Number & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = RoyalBlue800,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = patent.patentNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue800
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg,
                    border = BorderStroke(1.dp, statusBorder)
                ) {
                    Text(
                        text = patent.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = patent.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy900,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Applicant Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Applicant: ${patent.applicant}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate600,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Abstract / Summary
            patent.abstract?.let { abs ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = abs,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate600,
                        lineHeight = 16.sp
                    ),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Text(
                        text = "Jurisdiction: ${patent.jurisdiction}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Navy800
                        )
                    )
                }
                patent.filingDate?.let { fDate ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Slate100,
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Text(
                            text = "Filed: $fDate",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Slate600
                            )
                        )
                    }
                }
            }
        }
    }
}
