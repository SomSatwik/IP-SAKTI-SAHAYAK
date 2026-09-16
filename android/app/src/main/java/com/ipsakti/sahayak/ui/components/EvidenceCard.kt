package com.ipsakti.sahayak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.model.EvidenceItem
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun EvidenceCard(
    evidence: EvidenceItem,
    onClick: () -> Unit,
    onViewVersionHistory: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val relPercentage = (evidence.relevanceScore * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Document Name & Relevance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evidence.source.documentName.ifEmpty { evidence.title },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    )
                    evidence.source.authority?.let { auth ->
                        Text(
                            text = auth,
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = RoyalBlue800.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, RoyalBlue800.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Relevance $relPercentage%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalBlue800,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Row: Section, Page, Jurisdiction, Version
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                evidence.source.section?.let { sec ->
                    MetaBadge(label = "Sec", value = sec)
                }
                evidence.source.page?.let { pg ->
                    MetaBadge(label = "Page", value = pg)
                }
                evidence.source.jurisdiction?.let { jur ->
                    MetaBadge(label = "Jurisdiction", value = jur)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary / Excerpt
            Text(
                text = evidence.summary.ifEmpty { evidence.source.content ?: "" },
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate600),
                maxLines = 3
            )

            // Time Machine Version History Button
            if (onViewVersionHistory != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            val targetId = evidence.id.ifEmpty { evidence.source.documentName }
                            onViewVersionHistory(targetId)
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = RoyalBlue800,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View version history",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RoyalBlue800,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Button to View Full Supporting Evidence
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate100,
                    contentColor = RoyalBlue800
                ),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Inspect Supporting Passage & Source",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Slate100,
        border = BorderStroke(1.dp, Slate200)
    ) {
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Navy800
            )
        )
    }
}
