package com.ipsakti.sahayak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun AbstentionCard(
    message: String,
    evidenceFoundCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Gold600.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, Gold600.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Gold600,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "VERIFICATION INSUFFICIENT — SAFE ABSTENTION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gold700,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = message.ifEmpty {
                    "I could not find sufficient authoritative evidence in the indexed Indian IP & regulatory knowledge base to provide a reliable, grounded answer. The system abstains rather than generating speculative legal statements."
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = Navy900)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Gold600.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sources considered: $evidenceFoundCount",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                )
                Text(
                    text = "Evidence strength: Low",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = RiskHigh,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Recommendation: Consult with an accredited Indian Patent Agent or Ministry of Ayush regulatory officer.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = Navy800,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
