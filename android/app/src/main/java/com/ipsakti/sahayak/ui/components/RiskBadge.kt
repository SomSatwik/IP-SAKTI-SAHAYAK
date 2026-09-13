package com.ipsakti.sahayak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
fun RiskBadge(
    riskText: String,
    modifier: Modifier = Modifier
) {
    val isHigh = riskText.contains("High", ignoreCase = true)
    val isMedium = riskText.contains("Medium", ignoreCase = true)

    val (bgColor, borderColor, textColor) = when {
        isHigh -> Triple(RiskHighBg, RiskHigh.copy(alpha = 0.4f), RiskHigh)
        isMedium -> Triple(RiskMediumBg, RiskMedium.copy(alpha = 0.4f), RiskMedium)
        else -> Triple(RiskLowBg, RiskLow.copy(alpha = 0.4f), RiskLow)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = riskText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }
    }
}
