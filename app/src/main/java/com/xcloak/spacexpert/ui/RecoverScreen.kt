package com.xcloak.spacexpert.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.spacexpert.data.TrashEntity
import com.xcloak.spacexpert.ui.components.GlassCard
import com.xcloak.spacexpert.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecoverScreen(viewModel: RecoverViewModel = hiltViewModel()) {
    val items by viewModel.trashedItems.collectAsState()
    val colors = LocalSpaceXpertColors.current

    Box(Modifier.fillMaxSize().background(colors.backgroundGradient)) {
        // Floating Ambient Cosmic Glow Nodes
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(StarGold.copy(alpha = 0.12f), Color.Transparent)))
        )

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Recover Sector",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = CyanAccent
            )
            Text(
                "Inspect recently deleted nodes and restore data cores",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(20.dp))

            if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Trash database is completely clear.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items.distinctBy { it.id }, key = { it.id }) { item ->
                    TrashRow(item = item, onRestore = { viewModel.restore(item) })
                }
            }
        }
    }
}

@Composable
fun TrashRow(item: TrashEntity, onRestore: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val filename = remember(item.originalPath) { item.originalPath.substringAfterLast("/") }

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = filename,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Deleted ${dateFormat.format(Date(item.deletedAt))} · ${item.sizeBytes / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onRestore,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    "Restore",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SpaceNavy900
                )
            }
        }
    }
}
