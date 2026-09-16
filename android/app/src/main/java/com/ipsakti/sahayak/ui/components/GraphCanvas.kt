package com.ipsakti.sahayak.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.model.EvidenceGraphResponse
import com.ipsakti.sahayak.data.model.GraphNode
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

fun getNodeColor(type: String): Color {
    return when (type.lowercase()) {
        "query" -> Color(0xFF6366F1)
        "domain" -> Color(0xFF2563EB)
        "source" -> Color(0xFF16A34A)
        "risk" -> Color(0xFFDC2626)
        "action" -> Color(0xFF7C3AED)
        "requirement" -> Color(0xFFD97706)
        "compliance" -> Color(0xFF059669)
        "database" -> Color(0xFF0284C7)
        else -> Color(0xFF64748B)
    }
}

/**
 * Reusable GraphCanvas supporting both interactive exploration (EvidenceGraphScreen)
 * and compact live-building animation (ChatScreen "thinking" state).
 */
@Composable
fun GraphCanvas(
    graph: EvidenceGraphResponse,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    isCompact: Boolean = false,
    visibleNodeCount: Int = graph.nodes.size,
    onNodeSelected: ((GraphNode) -> Unit)? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Edge animation progress states (when both nodes are visible, edge draws in)
    val edgeAnimations = graph.edges.map { edge ->
        val fromIdx = graph.nodes.indexOfFirst { it.id == edge.source }
        val toIdx = graph.nodes.indexOfFirst { it.id == edge.target }
        val isBothVisible = fromIdx != -1 && toIdx != -1 && fromIdx < visibleNodeCount && toIdx < visibleNodeCount
        animateFloatAsState(
            targetValue = if (isBothVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 280),
            label = "edge_${edge.source}_${edge.target}"
        )
    }

    // Node scale/fade animation states
    val nodeAnimations = (0 until graph.nodes.size).map { idx ->
        animateFloatAsState(
            targetValue = if (idx < visibleNodeCount) 1f else 0f,
            animationSpec = tween(durationMillis = 280),
            label = "node_$idx"
        )
    }

    // Node positions for interactive screen (1000x1000 coordinate space)
    val interactivePositions = remember(graph.nodes) {
        val positions = mutableMapOf<String, Offset>()
        val centerX = 500f
        val centerY = 500f
        val byType = graph.nodes.groupBy { it.type }
        val typeOrder = listOf("query", "domain", "source", "risk", "action", "requirement", "database", "compliance")

        var layer = 0
        typeOrder.forEach { type ->
            val nodesOfType = byType[type] ?: return@forEach
            val radius = 120f + layer * 160f
            nodesOfType.forEachIndexed { index, node ->
                val angle = (2 * Math.PI * index / nodesOfType.size) - Math.PI / 2
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                positions[node.id] = Offset(x, y)
            }
            layer++
        }
        positions
    }

    val canvasModifier = if (isInteractive) {
        modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.4f, 3f)
                    offset += pan
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val adjustedTap = Offset(
                        (tapOffset.x - offset.x) / scale,
                        (tapOffset.y - offset.y) / scale
                    )
                    graph.nodes.forEach { node ->
                        val pos = interactivePositions[node.id] ?: return@forEach
                        val dist = (adjustedTap - pos).getDistance()
                        if (dist < 40f) {
                            onNodeSelected?.invoke(node)
                            return@detectTapGestures
                        }
                    }
                }
            }
    } else {
        modifier
    }

    Canvas(modifier = canvasModifier) {
        val width = size.width
        val height = size.height

        // Calculate positions dynamically for compact mode to fit neatly
        val nodePositions = if (isCompact) {
            val positions = mutableMapOf<String, Offset>()
            val cX = width / 2f
            val cY = height / 2f
            if (graph.nodes.isNotEmpty()) {
                val centerNode = graph.nodes.first()
                positions[centerNode.id] = Offset(cX, cY)

                val remaining = graph.nodes.drop(1)
                val radiusX = width * 0.36f
                val radiusY = height * 0.34f
                remaining.forEachIndexed { index, node ->
                    val angle = (2 * Math.PI * index / remaining.size) - Math.PI / 2
                    val x = cX + radiusX * cos(angle).toFloat()
                    val y = cY + radiusY * sin(angle).toFloat()
                    positions[node.id] = Offset(x, y)
                }
            }
            positions
        } else {
            interactivePositions
        }

        drawIntoCanvas { canvas ->
            if (isInteractive) {
                canvas.save()
                canvas.translate(offset.x, offset.y)
                canvas.scale(scale, scale)
            }

            val nativeCanvas = canvas.nativeCanvas

            // 1. Draw Edges
            graph.edges.forEachIndexed { edgeIdx, edge ->
                val progress = edgeAnimations.getOrNull(edgeIdx)?.value ?: 1f
                if (progress > 0.02f) {
                    val from = nodePositions[edge.source] ?: return@forEachIndexed
                    val to = nodePositions[edge.target] ?: return@forEachIndexed
                    val currentTo = Offset(
                        from.x + (to.x - from.x) * progress,
                        from.y + (to.y - from.y) * progress
                    )

                    drawLine(
                        color = Slate300.copy(alpha = progress),
                        start = from,
                        end = currentTo,
                        strokeWidth = if (isCompact) 1.5f else (2f / scale)
                    )

                    if (!isCompact && progress >= 0.9f) {
                        val midX = (from.x + to.x) / 2
                        val midY = (from.y + to.y) / 2
                        nativeCanvas.drawText(
                            edge.relation,
                            midX,
                            midY - 8,
                            Paint().apply {
                                color = Slate500.toArgb()
                                textSize = 22f
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                            }
                        )
                    }
                }
            }

            // 2. Draw Nodes
            graph.nodes.forEachIndexed { idx, node ->
                val animProgress = nodeAnimations.getOrNull(idx)?.value ?: 1f
                if (animProgress > 0.02f) {
                    val pos = nodePositions[node.id] ?: return@forEachIndexed
                    val nodeColor = getNodeColor(node.type)

                    val baseRadius = if (isCompact) {
                        if (idx == 0) 18f else 14f
                    } else {
                        when (node.type.lowercase()) {
                            "query" -> 35f
                            "domain" -> 30f
                            else -> 25f
                        }
                    }
                    val currentRadius = baseRadius * animProgress

                    // Circle fill
                    drawCircle(
                        color = nodeColor.copy(alpha = animProgress),
                        radius = currentRadius,
                        center = pos
                    )

                    // Outer border
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.35f * animProgress),
                        radius = currentRadius + if (isCompact) 3f else 4f,
                        center = pos,
                        style = Stroke(width = if (isCompact) 1.5f else 2f)
                    )

                    // Label
                    val labelText = if (isCompact && node.label.length > 14) {
                        node.label.take(12) + ".."
                    } else {
                        node.label
                    }

                    nativeCanvas.drawText(
                        labelText,
                        pos.x,
                        pos.y + currentRadius + if (isCompact) 13f else 18f,
                        Paint().apply {
                            color = Navy900.toArgb()
                            textSize = if (isCompact) 20f else 24f
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                            isFakeBoldText = true
                            alpha = (255 * animProgress).toInt()
                        }
                    )
                }
            }

            if (isInteractive) {
                canvas.restore()
            }
        }
    }
}

/**
 * Compact, live-building animated Evidence Graph for the Chat "thinking" state.
 * Animates nodes fading/scaling in one by one every ~300ms, with connected edges
 * drawing in once both nodes appear, capped at ~2 seconds total.
 */
@Composable
fun LiveBuildingEvidenceGraph(
    modifier: Modifier = Modifier,
    graph: EvidenceGraphResponse = remember { IpSaktiRepository().getFallbackDemoInvestigation().graph }
) {
    val totalNodes = graph.nodes.size.coerceAtLeast(1)
    val stepInterval = (2000L / totalNodes).coerceIn(200L, 350L)

    var visibleNodeCount by remember { mutableIntStateOf(1) }

    LaunchedEffect(graph) {
        visibleNodeCount = 1
        for (i in 2..totalNodes) {
            delay(stepInterval)
            visibleNodeCount = i
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Gold600)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GROUNDING EVIDENCE GRAPH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Gold700,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate100
                ) {
                    Text(
                        text = "$visibleNodeCount of $totalNodes nodes mapped",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate600,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            GraphCanvas(
                graph = graph,
                isInteractive = false,
                isCompact = true,
                visibleNodeCount = visibleNodeCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Traversing Indian Patents Act § 3(p), Biodiversity Act § 6 & TKDL citations...",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate500,
                    fontSize = 11.sp
                )
            )
        }
    }
}
