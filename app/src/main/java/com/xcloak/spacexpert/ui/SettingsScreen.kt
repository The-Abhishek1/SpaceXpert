package com.xcloak.spacexpert.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.spacexpert.ui.components.AdvancedGlassCard
import com.xcloak.spacexpert.ui.components.GlassCard
import com.xcloak.spacexpert.ui.components.SpaceButton
import com.xcloak.spacexpert.ui.theme.*
import com.xcloak.spacexpert.util.BannerAdView
import com.xcloak.spacexpert.util.BillingManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val colors = LocalSpaceXpertColors.current
    val context = LocalContext.current
    val isPro by BillingManager.isProActive.collectAsState()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else SpaceNavy900
    val hintColor = if (isDark) Color.White.copy(alpha = 0.7f) else SpaceNavy900.copy(alpha = 0.7f)

    var isAutopilotEnabled by remember { mutableStateOf(true) }
    var cleanupInterval by remember { mutableStateOf("Daily") }
    var isChargingOnly by remember { mutableStateOf(false) }
    var isDeepCosmicScanEnabled by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(colors.backgroundGradient)) {
        // Ambient Glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 50.dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(NebulaPurple.copy(alpha = 0.15f), Color.Transparent)))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "SETTINGS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = CyanAccent
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanAccent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // PREMIUM UPGRADE CORE CARD
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderBrush = Brush.linearGradient(listOf(StarGold, CyanAccent))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Premium Core",
                                    tint = StarGold,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        if (isPro) "SPACEXPERT PRO ACTIVE" else "UPGRADE TO PREMIUM",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = StarGold
                                    )
                                    Text(
                                        if (isPro) "All advanced features unlocked" else "Unlock full automatic cleaning & complete ad-free scanning",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = hintColor
                                    )
                                }
                            }
                            
                            if (!isPro) {
                                Spacer(Modifier.height(12.dp))
                                Text("• 100% Ad-Free Experience Across App\n• Advanced Background Interval Scanning\n• Deep System Optimization Analysis", style = MaterialTheme.typography.labelSmall, color = hintColor)
                                Spacer(Modifier.height(14.dp))
                                SpaceButton(
                                    text = "Activate Pro Subscription",
                                    onClick = {
                                        if (context is Activity) {
                                            BillingManager.launchPurchaseFlow(context)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = StarGold,
                                    contentColor = SpaceNavy900
                                )
                            } else {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Premium Features Unlocked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "[Dev Toggle]",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.clickable { BillingManager.toggleProStateDebug() }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    AdvancedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Automatic Cleaning",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                                Text(
                                    "AI-driven background maintenance cycles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = hintColor
                                )
                            }
                        }
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            SettingsToggle(
                                title = "Enable Automatic Background Scanning",
                                description = "Automatically find and highlight unneeded cached files",
                                checked = isAutopilotEnabled,
                                onCheckedChange = { isAutopilotEnabled = it }
                            )
                            
                            HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "SCAN FREQUENCY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!isPro) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = StarGold, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("PRO ONLY", style = MaterialTheme.typography.labelSmall, color = StarGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Daily", "Weekly", "Monthly").forEach { interval ->
                                    FilterChip(
                                        selected = cleanupInterval == interval,
                                        onClick = {
                                            if (isPro) {
                                                cleanupInterval = interval
                                            } else {
                                                if (context is Activity) {
                                                    BillingManager.launchPurchaseFlow(context)
                                                }
                                            }
                                        },
                                        label = { Text(interval) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyanAccent,
                                            selectedLabelColor = SpaceNavy900
                                        )
                                    )
                                }
                            }
                            
                            HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                            SettingsToggle(
                                title = "Charging Mode Only",
                                description = "Preserve battery: only scan when phone is charging",
                                checked = isChargingOnly,
                                onCheckedChange = { isChargingOnly = it }
                            )

                            HorizontalDivider(color = colors.glassBorder.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                            // PREMIUM FEATURE: DEEP SYSTEM SCAN
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Deep System Scan Mode", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        if (!isPro) {
                                            Spacer(Modifier.width(6.dp))
                                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = StarGold, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text("Thoroughly check all hidden files for full space optimization", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Switch(
                                    checked = isDeepCosmicScanEnabled && isPro,
                                    onCheckedChange = {
                                        if (isPro) {
                                            isDeepCosmicScanEnabled = it
                                        } else {
                                            if (context is Activity) {
                                                BillingManager.launchPurchaseFlow(context)
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                                )
                            }
                        }
                    }
                }
                
                item {
                    SpaceButton(
                        text = "Save Settings & Schedule Cleaning",
                        onClick = { /* In a real app, schedule WorkManager here */ },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = CyanAccent
                    )
                }

                // Conditional Banner Ad at the bottom of the Settings List
                if (!isPro) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "SPONSORED COORDINATES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        )
                        BannerAdView(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
        )
    }
}
