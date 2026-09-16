package com.ipsakti.sahayak.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AbstentionCard(
    message: String,
    evidenceFoundCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Subtle entrance animation (fade + scale from 0.95 to 1.0, ~400ms)
    val animAlpha = remember { Animatable(0f) }
    val animScale = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
        launch {
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animAlpha.value
                scaleX = animScale.value
                scaleY = animScale.value
            },
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

            HorizontalDivider(color = Gold600.copy(alpha = 0.2f))

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

            Spacer(modifier = Modifier.height(12.dp))

            // Intentional Expert Consultation Action
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:expert@ipsakti.gov.in")
                        putExtra(Intent.EXTRA_SUBJECT, "Legal Expert Consultation Request")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Query Details:\n$message\n\nPlease advise on applicable patent & regulatory compliance."
                        )
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:1800110180")
                        }
                        try {
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Gold700),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold800)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Consult an Expert",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
