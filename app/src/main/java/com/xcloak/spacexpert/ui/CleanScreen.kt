package com.xcloak.spacexpert.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.spacexpert.data.DuplicateGroup
import com.xcloak.spacexpert.ui.components.AdvancedGlassCard
import com.xcloak.spacexpert.ui.components.GlassCard
import com.xcloak.spacexpert.ui.components.SpaceButton
import com.xcloak.spacexpert.ui.theme.*
import kotlin.random.Random


private enum class CleanMode(val title: String) {
    EXACT("Exact"), VIDEO("Videos"), SIMILAR("Similar"), MESSENGER("Messenger"), STASIS("Stasis")
}

@Composable
fun CleanScreen(viewModel: CleanViewModel = hiltViewModel()) {
    val duplicateGroups by viewModel.duplicateGroups.collectAsState()
    val videoDuplicateGroups by viewModel.videoDuplicateGroups.collectAsState()
    val similarGroups by viewModel.similarGroups.collectAsState()
    val messengerFiles by viewModel.messengerFiles.collectAsState()
    val stasisApps by viewModel.stasisApps.collectAsState()
    val isScanningExact by viewModel.isScanningExact.collectAsState()
    val isScanningVideos by viewModel.isScanningVideos.collectAsState()
    val isScanningSimilar by viewModel.isScanningSimilar.collectAsState()
    val isScanningMessenger by viewModel.isScanningMessenger.collectAsState()
    val isScanningStasis by viewModel.isScanningStasis.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()

    var mode by remember { mutableStateOf(CleanMode.EXACT) }
    var selectedGroupForPreview by remember { mutableStateOf<DuplicateGroup?>(null) }
    
    val configuration = LocalConfiguration.current
    val haptic = LocalHapticFeedback.current
    val isWideScreen = configuration.screenWidthDp > 600
    val colors = LocalSpaceXpertColors.current

    Box(Modifier.fillMaxSize().background(colors.backgroundGradient)) {
        // Floating Space Ambient Glow Nodes
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (-40).dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(NebulaPurple.copy(alpha = 0.12f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 80.dp)
                .blur(70.dp)
                .background(Brush.radialGradient(listOf(CyanAccent.copy(alpha = 0.12f), Color.Transparent)))
        )

        Row(Modifier.fillMaxSize()) {
            // Left Column / Main List Pane
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text(
                    "Clean Core",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = CyanAccent
                )
                Text(
                    "De-duplicate sectors and purge space waste",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))

                ScrollableTabRow(
                    selectedTabIndex = mode.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        if (mode.ordinal < tabPositions.size) {
                            Box(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[mode.ordinal])
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CyanAccent)
                            )
                        }
                    }
                ) {
                    CleanMode.entries.forEach { cleanMode ->
                        Tab(
                            selected = mode == cleanMode,
                            onClick = { mode = cleanMode; selectedGroupForPreview = null },
                            text = { 
                                Text(
                                    cleanMode.title,
                                    fontWeight = if (mode == cleanMode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (mode == cleanMode) CyanAccent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                ) 
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                when (mode) {
                    CleanMode.EXACT -> {
                        SpaceButton(
                            text = if (isScanningExact) "Scanning Exact Sector..." else "Scan for Exact Duplicates",
                            onClick = { viewModel.scanForDuplicatePhotos() },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = CyanAccent
                        )
                        Spacer(Modifier.height(16.dp))
                        if (duplicateGroups.isEmpty() && !isScanningExact) {
                            EmptySectorLayout("No exact duplicates found yet. Run a sector scan.")
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(duplicateGroups, key = { group -> group.hash }) { group ->
                                DuplicateGroupRow(
                                    group = group,
                                    isDeleting = isDeleting,
                                    isSelected = selectedGroupForPreview?.hash == group.hash,
                                    onDeleteDuplicates = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.deleteDuplicatesKeepingFirst(group, fromSimilar = false) 
                                    },
                                    onClick = { selectedGroupForPreview = group }
                                )
                            }
                        }
                    }
                    CleanMode.VIDEO -> {
                        SpaceButton(
                            text = if (isScanningVideos) "Scanning Video Sectors..." else "Scan for Duplicate Videos",
                            onClick = { viewModel.scanForDuplicateVideos() },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = NebulaPurple,
                            contentColor = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        if (videoDuplicateGroups.isEmpty() && !isScanningVideos) {
                            EmptySectorLayout("No duplicate video nodes detected. Run a scan.")
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(videoDuplicateGroups, key = { group -> group.hash }) { group ->
                                DuplicateGroupRow(
                                    group = group,
                                    isDeleting = isDeleting,
                                    isSelected = selectedGroupForPreview?.hash == group.hash,
                                    onDeleteDuplicates = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.deleteDuplicatesKeepingFirst(group, fromSimilar = false) 
                                    },
                                    onClick = { selectedGroupForPreview = group }
                                )
                            }
                        }
                    }
                    CleanMode.SIMILAR -> {
                        SpaceButton(
                            text = if (isScanningSimilar) "Analyzing Deep Perception..." else "Scan for Similar Photos",
                            onClick = { viewModel.scanForSimilarPhotos() },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = StarGold
                        )
                        Text(
                            "Catches screenshots, resized copies and recompressed images — this scan is deeper.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                        )
                        if (similarGroups.isEmpty() && !isScanningSimilar) {
                            EmptySectorLayout("No similar photo sets found yet.")
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(similarGroups, key = { group -> group.hash }) { group ->
                                DuplicateGroupRow(
                                    group = group,
                                    isDeleting = isDeleting,
                                    isSelected = selectedGroupForPreview?.hash == group.hash,
                                    onDeleteDuplicates = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.deleteDuplicatesKeepingFirst(group, fromSimilar = true) 
                                    },
                                    onClick = { selectedGroupForPreview = group }
                                )
                            }
                        }
                    }
                    CleanMode.MESSENGER -> {
                        SpaceButton(
                            text = if (isScanningMessenger) "Purging Messenger Sent Logs..." else "Scan Messenger Sent Media",
                            onClick = { viewModel.scanForMessengerSentFiles() },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MarsRed,
                            contentColor = Color.White
                        )
                        Text(
                            "Finds media you've sent in apps like WhatsApp. These are usually duplicate payload waste.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                        )

                        if (messengerFiles.isNotEmpty()) {
                            val totalSize = messengerFiles.sumOf { it.sizeBytes }
                            
                            // Sub-space Comms Breakdown Badge Card
                            AdvancedGlassCard(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                padding = 12.dp
                            ) {
                                val whatsappCount = messengerFiles.count { it.path.contains("whatsapp", ignoreCase = true) }
                                val telegramCount = messengerFiles.count { it.path.contains("telegram", ignoreCase = true) }
                                Column {
                                    Text("Hyper-wave Channel Telemetry", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                                    Spacer(Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("WhatsApp Sector Residue: $whatsappCount entries", style = MaterialTheme.typography.labelSmall)
                                        Text("Telegram Sector Residue: $telegramCount entries", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            SpaceButton(
                                text = "Purge All Messenger Waste (${totalSize / (1024 * 1024)} MB)",
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteMessengerFiles(messengerFiles) 
                                },
                                containerColor = DangerRed,
                                contentColor = Color.White,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                            )
                        }

                        if (messengerFiles.isEmpty() && !isScanningMessenger) {
                            EmptySectorLayout("No redundant messenger copies found.")
                        }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(messengerFiles, key = { file -> file.path }) { file ->
                                GlassCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                file.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val channelTag = if (file.path.contains("whatsapp", ignoreCase = true)) "WhatsApp Sector" else "Telegram Sector"
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = (if (channelTag.startsWith("WhatsApp")) SuccessGreen else CyanAccent).copy(alpha = 0.15f),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                ) {
                                                    Text(
                                                        text = channelTag,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        color = if (channelTag.startsWith("WhatsApp")) SuccessGreen else CyanAccent,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    "${file.sizeBytes / 1024} KB",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        IconButton(onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.deleteMessengerFiles(listOf(file)) 
                                        }) {
                                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = DangerRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    CleanMode.STASIS -> {
                        SpaceButton(
                            text = if (isScanningStasis) "Scanning Active Sectors..." else "Scan for Inactive Modules",
                            onClick = { viewModel.scanForStasisCandidates() },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = StarGold,
                            contentColor = SpaceNavy900
                        )
                        Text(
                            "Isolate heavy background modules and residual app debris to freeze them into stasis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                        )

                        if (stasisApps.isEmpty() && !isScanningStasis) {
                            EmptySectorLayout("No application debris or stasis candidates isolated.")
                        }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(stasisApps, key = { app -> app.packageName }) { app ->
                                GlassCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                app.appName,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = if (app.isDebrisLeftover) "Orphaned Debris Residual Sector" else "Inactive for ${app.daysInactive} cycles",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (app.isDebrisLeftover) MarsRed else CyanAccent
                                            )
                                        }
                                        Button(
                                            onClick = { 
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.engageStasisFor(app) 
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (app.isDebrisLeftover) DangerRed else CyanAccent)
                                        ) {
                                            Text(
                                                text = if (app.isDebrisLeftover) "Purge Debris" else "Freeze Stasis",
                                                color = SpaceNavy900,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Column / Preview Pane (Two-pane layout for tablets/widescreens)
            if (isWideScreen && selectedGroupForPreview != null) {
                val currentGroup = selectedGroupForPreview!!
                val wastedBytes = currentGroup.files.drop(1).sumOf { it.size }
                AdvancedGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        "Sector Telemetry Preview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyanAccent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Reclaimable payload space: ${wastedBytes / (1024 * 1024)} MB",
                        color = StarGold,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("All referenced entries in set:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentGroup.files) { file ->
                            GlassCard(modifier = Modifier.fillMaxWidth(), padding = 10.dp) {
                                Text(
                                    file.path,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Size: ${file.size / 1024} KB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (isDeleting) {
            WarpAnimationOverlay()
        }
    }
}

@Composable
fun WarpAnimationOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "warp")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(320.dp)) {
            val center = center
            val radius = size.minDimension / 2
            
            // Draw multi-layered gravitational distortion waves
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NebulaPurple.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = radius * progress
                ),
                radius = radius * progress
            )

            drawCircle(
                color = CyanAccent.copy(alpha = 0.5f * (1f - progress)),
                radius = radius * progress,
                style = Stroke(width = 3.dp.toPx())
            )
            
            drawCircle(
                color = NebulaPurple.copy(alpha = 0.6f * progress),
                radius = radius * (1f - progress),
                style = Stroke(width = 5.dp.toPx())
            )

            // Event horizon core singularity
            drawCircle(
                color = Color.Black,
                radius = 30.dp.toPx()
            )
            drawCircle(
                color = CyanAccent.copy(alpha = 0.3f),
                radius = 34.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Advanced Particle Vortices being drawn into the event horizon
            for (i in 0..45) {
                val initialAngle = i * 8.0
                val customAngle = initialAngle + spinAngle
                val particleProgress = (progress + (i % 3) * 0.33f) % 1f
                val particleRadius = radius * (1f - particleProgress)
                
                if (particleRadius > 30.dp.toPx()) {
                    val x = center.x + Math.cos(Math.toRadians(customAngle)) * particleRadius
                    val y = center.y + Math.sin(Math.toRadians(customAngle)) * particleRadius
                    
                    val particleColor = when (i % 3) {
                        0 -> CyanAccent
                        1 -> NebulaPurple
                        else -> StarGold
                    }

                    drawCircle(
                        color = particleColor,
                        radius = (1.5.dp + (3.5.dp * (1f - particleProgress))).toPx(),
                        center = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat()),
                        alpha = 1f - particleProgress
                    )
                }
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 380.dp)
        ) {
            Text(
                "ENGAGING WARP SINGULARITY",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = CyanAccent
            )
            Text(
                "Dematerializing space waste into sub-atomic nodes...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EmptySectorLayout(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun DuplicateGroupRow(
    group: DuplicateGroup,
    isDeleting: Boolean,
    isSelected: Boolean,
    onDeleteDuplicates: () -> Unit,
    onClick: () -> Unit
) {
    val wastedBytes = group.files.drop(1).sumOf { it.size }
    val glowBrush = if (isSelected) Brush.linearGradient(listOf(CyanAccent, NebulaPurple)) else null

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        padding = 14.dp,
        borderBrush = glowBrush
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StarGold)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${group.files.size} replicas detected",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "${wastedBytes / (1024 * 1024)} MB redundant payload waste",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarGold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                group.files.take(2).forEachIndexed { index, file ->
                    Text(
                        text = if (index == 0) "• ${file.path.substringAfterLast("/")} (keep)" else "• ${file.path.substringAfterLast("/")}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (index == 0) CyanAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (group.files.size > 2) {
                    Text(
                        "+${group.files.size - 2} more copies",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onDeleteDuplicates,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isDeleting) "Purging..." else "Purge rest",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}
