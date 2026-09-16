package com.ipsakti.sahayak.ui.screens.evidence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.EvidenceCard
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun EvidenceListScreen(
    onViewEvidence: (String) -> Unit,
    onViewVersionHistory: (String) -> Unit = {}
) {
    val repository = remember { IpSaktiRepository() }
    val demoInv = remember { repository.getFallbackDemoInvestigation() }
    val evidenceItems = remember { demoInv.response.evidence }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Evidence Repository",
                subtitle = "Authoritative Legal & Regulatory Sources"
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        if (evidenceItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No evidence indexed yet",
                        style = MaterialTheme.typography.titleMedium.copy(color = Slate600)
                    )
                    Text(
                        text = "Run an investigation to populate the evidence repository.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Text(
                            text = "${evidenceItems.size} authoritative sources indexed from Indian IP, TK, and regulatory frameworks.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate600,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                items(evidenceItems) { ev ->
                    EvidenceCard(
                        evidence = ev,
                        onClick = { onViewEvidence(ev.id) },
                        onViewVersionHistory = onViewVersionHistory
                    )
                }
            }
        }
    }
}
