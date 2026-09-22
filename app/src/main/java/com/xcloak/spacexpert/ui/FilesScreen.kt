package com.xcloak.spacexpert.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.spacexpert.data.BrowsableFile
import com.xcloak.spacexpert.ui.components.AdvancedGlassCard
import com.xcloak.spacexpert.ui.components.GlassCard
import com.xcloak.spacexpert.ui.theme.*

@Composable
fun FilesScreen(viewModel: FilesViewModel = hiltViewModel()) {
    val currentPath by viewModel.currentPath.collectAsState()
    val entries by viewModel.entries.collectAsState()
    var pendingDelete by remember { mutableStateOf<BrowsableFile?>(null) }
    var selectedFileForPreview by remember { mutableStateOf<BrowsableFile?>(null) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp > 600
    val colors = LocalSpaceXpertColors.current

    BackHandler(enabled = true) {
        viewModel.goBack()
    }

    Box(Modifier.fillMaxSize().background(colors.backgroundGradient)) {
        // Floating Ambient Cosmic Glow Nodes
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .blur(90.dp)
                .background(Brush.radialGradient(listOf(CometBlue.copy(alpha = 0.15f), Color.Transparent)))
        )

        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text(
                    "File Explorer",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = CyanAccent
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Text(
                        currentPath,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))

                if (entries.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "This folder sector is empty.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                // distinctBy to ensure no lazy key duplication errors present
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(entries.distinctBy { it.path }, key = { it.path }) { file ->
                        FileEntryRow(
                            file = file,
                            isSelected = selectedFileForPreview?.path == file.path,
                            onClick = {
                                if (file.isDirectory) {
                                    viewModel.openFolder(file.path)
                                    selectedFileForPreview = null
                                } else {
                                    selectedFileForPreview = file
                                }
                            },
                            onDeleteClick = { pendingDelete = file }
                        )
                    }
                }
            }

            if (isWideScreen && selectedFileForPreview != null) {
                val file = selectedFileForPreview!!
                AdvancedGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        "File Telemetry Inspection",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyanAccent
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Name:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        file.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Sector Path:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        file.path,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Payload Weight:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "${file.sizeBytes / 1024} KB (~${file.sizeBytes / (1024 * 1024)} MB)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = StarGold
                    )
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { pendingDelete = file },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Purge Sector File", color = Color.White)
                    }
                }
            }
        }
    }

    pendingDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete permanently?") },
            text = { Text("${file.name} will be deleted immediately — this bypasses trash and cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePermanently(file)
                    if (selectedFileForPreview?.path == file.path) {
                        selectedFileForPreview = null
                    }
                    pendingDelete = null
                }) { Text("Delete", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FileEntryRow(
    file: BrowsableFile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val borderBrush = if (isSelected) Brush.linearGradient(listOf(CyanAccent, CometBlue)) else null
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        padding = 12.dp,
        borderBrush = borderBrush
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = if (file.isDirectory) CyanAccent else TextSecondaryDark
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        file.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (!file.isDirectory) {
                        Text(
                            "${file.sizeBytes / 1024} KB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            "Directory Sector",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            if (!file.isDirectory) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = DangerRed.copy(alpha = 0.8f))
                }
            }
        }
    }
}
