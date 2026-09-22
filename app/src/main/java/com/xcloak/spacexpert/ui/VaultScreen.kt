package com.xcloak.spacexpert.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.LocalActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.spacexpert.data.VaultEntity
import com.xcloak.spacexpert.ui.components.AdvancedGlassCard
import com.xcloak.spacexpert.ui.components.GlassCard
import com.xcloak.spacexpert.ui.components.SpaceButton
import com.xcloak.spacexpert.ui.theme.*

@Composable
fun VaultScreen(viewModel: VaultViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val isDecoyMode by viewModel.isDecoyMode.collectAsState()
    val items by viewModel.vaultItems.collectAsState()
    val colors = LocalSpaceXpertColors.current

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val name = queryDisplayName(context, it) ?: "vault_item"
            viewModel.addToVault(it, name, originalPath = null)
        }
    }

    Box(Modifier.fillMaxSize().background(colors.backgroundGradient)) {
        // Shared Floating Cosmic Ambient Glow Nodes
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-100).dp, y = 40.dp)
                .blur(90.dp)
                .background(Brush.radialGradient(listOf(NebulaPurple.copy(alpha = 0.15f), Color.Transparent)))
        )

        if (!isUnlocked) {
            VaultLockScreen(onUnlocked = { isDecoy -> viewModel.onUnlocked(isDecoy) })
        } else {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isDecoyMode) "Phantom Sector Core" else "Secure Vault Core",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isDecoyMode) StarGold else CyanAccent
                        )
                        Text(
                            if (isDecoyMode) "Decoy Mode: Public Sector Emulation" else "Sector Files: AES-256 Multi-Layer Encryption",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { viewModel.lockVault() }) {
                        Icon(
                            imageVector = if (isDecoyMode) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = "Lock Vault",
                            tint = if (isDecoyMode) StarGold else CyanAccent
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                SpaceButton(
                    text = if (isDecoyMode) "Encryption Offline in Decoy Mode" else "Encrypt New File into Vault",
                    onClick = { if (!isDecoyMode) pickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (isDecoyMode) Color.Gray.copy(alpha = 0.3f) else CyanAccent
                )
                
                Spacer(Modifier.height(24.dp))

                if (items.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No encrypted entries in this sector.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items.distinctBy { it.id }, key = { it.id }) { item ->
                        VaultItemRow(
                            item = item,
                            isDecoy = isDecoyMode,
                            onRemove = { viewModel.removeFromVault(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VaultItemRow(item: VaultEntity, isDecoy: Boolean, onRemove: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 14.dp,
        borderBrush = if (isDecoy) Brush.linearGradient(listOf(StarGold.copy(alpha = 0.4f), Color.Transparent)) else null
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.originalName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    if (isDecoy) "Decoy Payload Node" else "${item.sizeBytes / 1024} KB · Encrypted Payload",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onRemove, enabled = !isDecoy) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove from vault",
                    tint = if (isDecoy) Color.Gray.copy(alpha = 0.4f) else DangerRed.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun VaultLockScreen(onUnlocked: (Boolean) -> Unit) {
    val activity = LocalActivity.current as? FragmentActivity
    var showPasscodeDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val scannerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerAlpha"
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AdvancedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            padding = 40.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    // Fingerprint / Lock Icon with scanning effect
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scale),
                        tint = CyanAccent
                    )
                    
                    // Scanning line effect
                    Box(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(2.dp)
                            .align(Alignment.Center)
                            .offset(y = (40 * scannerAlpha).dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, CyanAccent, Color.Transparent)))
                            .blur(2.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    "Cosmo Vault Portal",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Biometric authentication required to decrypt storage sector.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(48.dp))
                
                SpaceButton(
                    text = "Authenticate Biometrics",
                    onClick = { activity?.let { promptBiometric(it) { onUnlocked(false) } } },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = CyanAccent
                )
                
                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { showPasscodeDialog = true }) {
                    Text(
                        "Manual Sector Override",
                        color = CyanAccent.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Locked with Quantum AES-256",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }

    if (showPasscodeDialog) {
        PasscodeOverrideDialog(
            onDismiss = { showPasscodeDialog = false },
            onPasscodeEntered = { pin ->
                when (pin) {
                    "1111" -> { onUnlocked(false); showPasscodeDialog = false }
                    "0000" -> { onUnlocked(true); showPasscodeDialog = false }
                }
            }
        )
    }
}

@Composable
fun PasscodeOverrideDialog(onDismiss: () -> Unit, onPasscodeEntered: (String) -> Unit) {
    var passcode by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Sector Pin Code") },
        text = {
            Column {
                Text(
                    "Input quantum-encrypted access key to bypass biometric scan.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = passcode,
                    onValueChange = { if (it.length <= 4) passcode = it },
                    label = { Text("Sector PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        cursorColor = CyanAccent
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPasscodeEntered(passcode) },
                enabled = passcode.length == 4
            ) {
                Text("Engage Access", color = CyanAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun promptBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
    val biometricManager = BiometricManager.from(activity)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    val canAuth = biometricManager.canAuthenticate(authenticators)

    if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
        if (canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
             onSuccess()
             return
        }
        onSuccess()
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Vault Access")
        .setSubtitle("Authenticate to enter the secure portal")
        .setAllowedAuthenticators(authenticators)
        .build()

    prompt.authenticate(promptInfo)
}

private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
    }
}
