package com.xcloak.spacexpert.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.spacexpert.data.CategorySummary
import com.xcloak.spacexpert.data.FileCategory
import com.xcloak.spacexpert.data.LargeFile
import com.xcloak.spacexpert.engine.AIRecommendation
import com.xcloak.spacexpert.engine.RecommendationType
import com.xcloak.spacexpert.ui.components.AdvancedGlassCard
import com.xcloak.spacexpert.ui.components.GlassCard
import com.xcloak.spacexpert.ui.components.SpaceButton
import com.xcloak.spacexpert.ui.theme.*
import java.util.Locale
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers


@Composable
fun HomeScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val summaries by viewModel.categorySummaries.collectAsState()
    val largestFiles by viewModel.largestFiles.collectAsState()
    val cleanupBytes by viewModel.potentialCleanupBytes.collectAsState()
    val aiRecommendations by viewModel.aiRecommendations.collectAsState()
    val isAnalyzingAI by viewModel.isAnalyzingAI.collectAsState()
    val deletingPaths by viewModel.deletingPaths.collectAsState()
    val chronoFilterDays by viewModel.chronoFilterDays.collectAsState()
    val cloudNodes by viewModel.cloudNodes.collectAsState()
    val totalUsed = summaries.sumOf { it.sizeBytes }
    val colors = LocalSpaceXpertColors.current
    val isPro by com.xcloak.spacexpert.util.BillingManager.isProActive.collectAsState()

    var pendingDelete by remember { mutableStateOf<LargeFile?>(null) }
    var activePreviewFile by remember { mutableStateOf<LargeFile?>(null) }


    Box(Modifier.fillMaxSize().background(colors.backgroundGradient)) {
        // Futuristic Ambient Cosmic Glow Nodes
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(CyanAccent.copy(alpha = 0.15f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 100.dp)
                .blur(70.dp)
                .background(Brush.radialGradient(listOf(NebulaPurple.copy(alpha = 0.15f), Color.Transparent)))
        )

        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.xcloak.spacexpert.R.drawable.spacexpert_glyph_transparent),
                            contentDescription = "SpaceXpert Glyph",
                            modifier = Modifier.size(48.dp).padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                "SpaceXpert",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = CyanAccent
                            )
                            Text(
                                "System Telemetry & Storage Core",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // System health monitor badge
                    val isStorageHigh = totalUsed > 20_000_000_000L // 20GB threshold placeholder
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SystemCoreBadge(isActionRequired = isStorageHigh || cleanupBytes > 500 * 1024 * 1024)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                AdvancedGlassCard(Modifier.fillMaxWidth()) {
                    StorageDonutChart(summaries, totalUsed)
                    Spacer(Modifier.height(16.dp))
                    
                    // Linear distribution graph representation
                    StorageLinearGraph(summaries, totalUsed)
                }

                Spacer(Modifier.height(20.dp))
            }

            item {
                AnimatedVisibility(
                    visible = cleanupBytes > 0 || aiRecommendations.isNotEmpty() || isAnalyzingAI,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        if (isAnalyzingAI) {
                            AIAnalyzingCard()
                        } else {
                            aiRecommendations.forEach { recommendation ->
                                AIRecommendationCard(recommendation)
                            }
                        }
                        
                        if (cleanupBytes > 0) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                borderBrush = Brush.linearGradient(listOf(StarGold, MarsRed))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Cleanup Alert",
                                        tint = StarGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Potential cleanup: ${cleanupBytes / (1024 * 1024)} MB",
                                            fontWeight = FontWeight.Bold,
                                            color = StarGold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            "Old downloads (180+ days) and obsolete cache items detected.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Storage Category Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                GlassCard(Modifier.fillMaxWidth()) {
                    summaries.forEachIndexed { index, summary ->
                        CategoryRow(summary, totalUsed)
                        if (index < summaries.size - 1) {
                            HorizontalDivider(
                                color = colors.glassBorder.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Galactic Cloud Telemetry",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    cloudNodes.forEach { node ->
                        GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(node.providerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(node.connectedAccount, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    Text(
                                        text = "${node.orphanPayloadsCount} duplicate vectors",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StarGold
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                val progress = if (node.totalSpaceBytes > 0) node.usedSpaceBytes.toFloat() / node.totalSpaceBytes.toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = NebulaPurple,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${node.usedSpaceBytes / (1024 * 1024 * 1024)} GB Used", style = MaterialTheme.typography.labelSmall)
                                    Text("${node.totalSpaceBytes / (1024 * 1024 * 1024)} GB Total", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                AdvancedGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column {
                        Text(
                            "Temporal Distortion Radar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                        Text(
                            "Filter file payload accumulation by time-horizon velocity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(14.dp))
                        
                        Slider(
                            value = chronoFilterDays,
                            onValueChange = { viewModel.updateChronoFilter(it) },
                            valueRange = 7f..365f,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("7 Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(
                                "Chrono-Filter: Max ${chronoFilterDays.toInt()} Days Old",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = StarGold
                            )
                            Text("365 Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            item {
                Text(
                    "Largest Files Sector",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Tap file card to preview & inspect details before moving to trash",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(largestFiles.distinctBy { it.path }, key = { it.path }) { file ->
                LargeFileRow(
                    file = file,
                    isDeleting = deletingPaths.contains(file.path),
                    onCardClick = { activePreviewFile = file },
                    onDeleteClick = { pendingDelete = file }
                )
            }

            if (!isPro) {
                item {
                    Spacer(Modifier.height(16.dp))
                    com.xcloak.spacexpert.util.BannerAdView(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Requested Feature: File Preview & Inspection Dialog
    val isCompressing by viewModel.isCompressing.collectAsState()
    
    activePreviewFile?.let { file ->
        FilePreviewDialog(
            file = file,
            isCompressing = isCompressing,
            onDismiss = { if (!isCompressing) activePreviewFile = null },
            onTrashClick = {
                pendingDelete = file
                activePreviewFile = null
            },
            onCompressClick = {
                viewModel.compressFile(file) {
                    activePreviewFile = null
                }
            }
        )
    }

    pendingDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Move to trash?") },
            text = { Text("${file.name} will move to trash and can be restored within 7 days.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteToTrash(file)
                    pendingDelete = null
                }) { Text("Trash it", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

}

@Composable
fun AIAnalyzingCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        borderBrush = Brush.linearGradient(listOf(CyanAccent, NebulaPurple))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(CyanAccent.copy(alpha = alpha), Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = CyanAccent,
                    strokeWidth = 2.dp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "AI Quantum Analyzer Active",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyanAccent
                )
                Text(
                    "Scanning deep sectors for redundant patterns...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AIRecommendationCard(recommendation: AIRecommendation) {
    val colors = when (recommendation.type) {
        RecommendationType.BLURRY_PHOTOS -> listOf(CyanAccent, CometBlue)
        RecommendationType.MEMES -> listOf(StarGold, MarsRed)
        RecommendationType.LARGE_SCREEN_RECORDINGS -> listOf(NebulaPurple, CyanAccent)
        RecommendationType.DUPLICATE_PHOTOS -> listOf(SuccessGreen, CyanAccent)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        borderBrush = Brush.linearGradient(colors)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = colors.first().copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.first(),
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    recommendation.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.first()
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                recommendation.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mb = recommendation.potentialSavingsBytes / (1024 * 1024)
                Text(
                    "Warp clean to save ${mb}MB",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = StarGold
                )
                SpaceButton(
                    text = "Warp Clean",
                    onClick = { /* Action handled by CleanScreen usually */ },
                    containerColor = colors.first(),
                    contentColor = SpaceNavy900,
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

@Composable
fun SystemCoreBadge(isActionRequired: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActionRequired) MarsRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f),
        modifier = Modifier.border(
            1.dp,
            if (isActionRequired) MarsRed.copy(alpha = 0.6f) else SuccessGreen.copy(alpha = 0.6f),
            RoundedCornerShape(12.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActionRequired) MarsRed else SuccessGreen)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isActionRequired) "CORE: BURDENED" else "CORE: OPTIMAL",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = if (isActionRequired) MarsRed else SuccessGreen
            )
        }
    }
}

@Composable
fun StorageDonutChart(summaries: List<CategorySummary>, totalUsed: Long) {
    val strokeWidth = 20.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            if (totalUsed == 0L) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            } else {
                var startAngle = -90f
                summaries.forEach { summary ->
                    val sweepAngle = (summary.sizeBytes.toFloat() / totalUsed.toFloat()) * 360f
                    // Underlay glow arc
                    drawArc(
                        color = getColorForCategory(summary.category).copy(alpha = 0.25f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = (strokeWidth + 6.dp).toPx(), cap = StrokeCap.Round)
                    )
                    // Primary premium sharp arc
                    drawArc(
                        color = getColorForCategory(summary.category),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val totalGB = totalUsed / (1024.0 * 1024.0 * 1024.0)
            Text(
                text = String.format(Locale.getDefault(), "%.1f", totalGB),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                color = CyanAccent
            )
            Text(
                text = "GB USED",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun StorageLinearGraph(summaries: List<CategorySummary>, totalUsed: Long) {
    if (totalUsed == 0L) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Linear Core Distribution",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            summaries.forEach { summary ->
                val weight = (summary.sizeBytes.toFloat() / totalUsed.toFloat()).coerceAtLeast(0.01f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(weight)
                        .background(getColorForCategory(summary.category))
                )
            }
        }
    }
}

private fun getColorForCategory(category: FileCategory): Color {
    return when (category) {
        FileCategory.PHOTOS -> PhotosCyan
        FileCategory.VIDEOS -> NebulaPurple
        FileCategory.DOCUMENTS -> CometBlue
        FileCategory.DOWNLOADS -> StarGold
        FileCategory.AUDIO -> MarsRed
        FileCategory.OTHER -> AsteroidGray
    }
}

@Composable
fun CategoryRow(summary: CategorySummary, totalUsed: Long) {
    val percentage = if (totalUsed > 0) (summary.sizeBytes.toFloat() / totalUsed * 100).toInt() else 0
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(getColorForCategory(summary.category))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    summary.category.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "${summary.count} files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${summary.sizeBytes / (1024 * 1024)} MB",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = CyanAccent
            )
        }
    }
}

@Composable
fun LargeFileRow(
    file: LargeFile,
    isDeleting: Boolean,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onCardClick() },
        padding = 14.dp
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    file.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = file.path.substringAfterLast(".", "FILE"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 4.getExtPadding(), vertical = 2.getExtPadding())
                        )
                    }
                    Text(
                        "${file.sizeBytes / (1024 * 1024)} MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            if (isDeleting) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = CyanAccent)
            } else {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Move to trash",
                        tint = DangerRed.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

private fun Int.getExtPadding() = this.dp

@Composable
fun AudioPreviewComponent(file: LargeFile) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.3f) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 16.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { isPlaying = !isPlaying }) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = CyanAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        "Quantum Audio Transmission",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent
                    )
                    Slider(
                        value = progress,
                        onValueChange = { progress = it },
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
                
                Text(
                    "2:45",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = file.path.substringAfterLast("/"),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FilePreviewDialog(
    file: LargeFile,
    isCompressing: Boolean,
    onDismiss: () -> Unit,
    onTrashClick: () -> Unit,
    onCompressClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (isCompressing) "Engaging Squeeze Matrix" else "Advanced Sector Inspection",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                if (isCompressing) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = StarGold, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Re-mapping coordinate vectors...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarGold
                        )
                    }
                } else {
                    Text(
                        text = "File Telemetry:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Absolute Core Path:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = file.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Payload Size:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${file.sizeBytes} Bytes (~${file.sizeBytes / (1024 * 1024)} MB)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyanAccent
                    )

                    Spacer(Modifier.height(16.dp))
                    
                    val isImage = remember(file.path) {
                        val ext = file.path.substringAfterLast(".", "").lowercase()
                        ext in listOf("jpg", "jpeg", "png", "webp", "bmp")
                    }
                    val isVideo = remember(file.path) {
                        val ext = file.path.substringAfterLast(".", "").lowercase()
                        ext in listOf("mp4", "mkv", "avi", "mov", "webp")
                    }
                    val isAudio = remember(file.path) {
                        val ext = file.path.substringAfterLast(".", "").lowercase()
                        ext in listOf("mp3", "wav", "m4a", "ogg")
                    }
                    
                    if (isImage) {
                        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                        LaunchedEffect(file.path) {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    android.graphics.BitmapFactory.decodeFile(file.path)
                                }
                            }.onSuccess { bitmap = it }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap!!.asImageBitmap(),
                                    contentDescription = "File Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(24.dp))
                            }
                        }
                    } else if (isAudio) {
                        AudioPreviewComponent(file)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isVideo) "[ Quantum Video Preview Stream Ready ]" else "[ Deep Holographic Preview Unavailable ]",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val ext = file.path.substringAfterLast(".", "").lowercase()
                val canCompress = ext in listOf("jpg", "jpeg", "png", "webp", "mp4", "mkv", "mov")
                
                if (canCompress && !isCompressing) {
                    Button(
                        onClick = onCompressClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StarGold)
                    ) {
                        Text("Matrix Squeeze", color = SpaceNavy900, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (!isCompressing) {
                    Button(
                        onClick = onTrashClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Trash File", color = Color.White)
                    }
                }
            }
        },
        dismissButton = {
            if (!isCompressing) {
                TextButton(onClick = onDismiss) {
                    Text("Close Inspection")
                }
            }
        }
    )
}
